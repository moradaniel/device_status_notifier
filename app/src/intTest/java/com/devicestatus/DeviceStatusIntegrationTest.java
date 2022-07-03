package com.devicestatus;


import com.devicestatus.domain.Device;
import com.devicestatus.domain.DeviceStatus;
import com.devicestatus.domain.DeviceStatusEventAvro;
import com.devicestatus.service.DeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.TopicExistsException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Profile("intTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeviceStatusIntegrationTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    DeviceService deviceService;

    @Value("${kafka.topics.device-status-events}")
    private String deviceStatusEventsTopic;

    @Value("${kafka.topics.device-status-change-notify}")
    private String deviceStatusChangeNotifyTopic;

    @Autowired
    private KafkaAdmin admin;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, DeviceStatusEventAvro> kafkaDeviceStatusEventConsumerFactory;

    @Autowired
    private ConsumerFactory<String, DeviceStatusEventAvro> kafkaPushNotificationConsumerFactory;

    private static final long POLL_INTERVAL_MS = 100L;
    private static final long POLL_TIMEOUT_MS = 9_000L;

    @Autowired
    Environment env;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    //https://github.com/SolaceProducts/pubsubplus-connector-kafka-sink/blob/29abade80dd0d359b8e51b43bd6b1a584d2abb62/src/integrationTest/java/com/solace/connector/kafka/connect/sink/it/MessagingServiceFullLocalSetupApache.java
    @Container
    public static final DockerComposeContainer COMPOSE_CONTAINER_KAFKA = new DockerComposeContainer(
            new File("../docker/docker-compose-kafka.yml"))
            .withExposedService("broker", 9092, Wait.forListeningPort())
            .withLocalCompose(true);

    @Container
    public static final DockerComposeContainer COMPOSE_CONTAINER_POSTGRES = new DockerComposeContainer(
            new File("src/intTest/resources/docker/docker-compose-postgres-test.yml"))
            .withLocalCompose(true);

    @Container
    public static final DockerComposeContainer COMPOSE_CONTAINER_MONGO = new DockerComposeContainer(
            new File("../docker/docker-compose-mongo-dev.yml"))
            .withLocalCompose(true);

    @Container
    public static final DockerComposeContainer COMPOSE_CONTAINER_WIREMOCK = new DockerComposeContainer(
            new File("src/intTest/resources/docker/docker-compose-wiremock-test.yml"))
            .withLocalCompose(true);

    static {
        COMPOSE_CONTAINER_KAFKA.start();
        COMPOSE_CONTAINER_POSTGRES.start();
        COMPOSE_CONTAINER_MONGO.start();
    }

    @BeforeAll
    public void setupDatabase() throws Exception {
        createTestDatabase();
    }


    @BeforeEach
    public void setup() throws Exception {

        cleanTopics();
        configureTopics();
        cleanTestDatabase();

        deviceService.save(new Device("router", DeviceStatus.down));

    }

    protected void configureTopics(/*String bootstrapServers, int partitions, int rf*/) throws Exception {
        try (AdminClient adminClient = AdminClient.create(admin.getConfigurationProperties())) {

            int partitions = 1;
            short replicationFactor = 1;

            Collection<NewTopic> topics = List.of(new NewTopic(deviceStatusEventsTopic, partitions, replicationFactor),
                    new NewTopic(deviceStatusChangeNotifyTopic, partitions, replicationFactor));
            try {
                adminClient.createTopics(topics).all().get(30, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException e) {
                if (!(e.getCause() instanceof TopicExistsException)) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void cleanTopics() throws Exception {
        try (AdminClient adminClient = AdminClient.create(admin.getConfigurationProperties())) {
            Set<String> applicationTopics = Set.of(deviceStatusEventsTopic, deviceStatusChangeNotifyTopic);
            List<String> topicsToDelete = new ArrayList<>();
            Collection<TopicListing> existingTopicList = adminClient.listTopics().listings().get();
            for (TopicListing existingTopic : existingTopicList) {
                if (applicationTopics.contains(existingTopic.name())) {
                    topicsToDelete.add(existingTopic.name());
                }
            }

            final AtomicInteger deletedTopics = new AtomicInteger();
            await().atMost(8, TimeUnit.SECONDS).pollInterval(Duration.ofSeconds(1)).until(() -> {
                for (String topicToDelete : topicsToDelete) {
                    adminClient.deleteTopics(Collections.singleton(topicToDelete));
                    deletedTopics.getAndIncrement();
                }
                return deletedTopics.get() == topicsToDelete.size();
            });
        }
    }


    @Test
    @DisplayName("down -> alerted - should generate push notification")
    public void test_down_alerted_GeneratesPushNotificaction() throws Exception {

        DeviceStatusEventAvro deviceStatusEventAvro = new DeviceStatusEventAvro("1", DeviceStatus.alerted.name());


        kafkaTemplate.send(deviceStatusEventsTopic, deviceStatusEventAvro.getDeviceId(), deviceStatusEventAvro);

        //assert that we can consume the generated deviceStatusEvent from Kafka
        List<ConsumerRecord<String, DeviceStatusEventAvro>> deviceStatusEventConsumerRecords = dumpTopic(kafkaDeviceStatusEventConsumerFactory, deviceStatusEventsTopic, 1, POLL_TIMEOUT_MS);
        assertThat(deviceStatusEventConsumerRecords.size()).isEqualTo(1);

        final AtomicReference<Device> resultContainer = new AtomicReference();
        await().atMost(10, TimeUnit.SECONDS).pollInterval(Duration.ofSeconds(2)).until(() -> {
            Device savedDevice = deviceService.findByDeviceId(Long.valueOf(deviceStatusEventAvro.getDeviceId())).get();
            if (savedDevice.getStatus().equals(DeviceStatus.valueOf(deviceStatusEventAvro.getStatus()))) {
                resultContainer.set(savedDevice);
                return true;
            }

            return false;
        });

        assertThat(resultContainer.get().getStatus()).isEqualTo(DeviceStatus.valueOf(deviceStatusEventAvro.getStatus()));


        //assert that we can consume the generated pushNotification from Kafka
        List<ConsumerRecord<String, DeviceStatusEventAvro>> pushNotificationConsumerRecords = dumpTopic(kafkaPushNotificationConsumerFactory, deviceStatusChangeNotifyTopic, 1, POLL_TIMEOUT_MS);
        assertThat(pushNotificationConsumerRecords.size()).isEqualTo(1);

    }

    /**
     * https://github.com/findinpath/testcontainers-kafka-avro/blob/943ae540e8db36a7515e5b454b71c7fdd54ea4b0/src/test/java/com/findinpath/AvroDemoTest.java
     *
     * @param kafkaConsumerFactory
     * @param topic
     * @param minMessageCount
     * @param pollTimeoutMillis
     * @param <T>
     * @return
     */
    private <T> List<ConsumerRecord<String, T>> dumpTopic(ConsumerFactory<String, T> kafkaConsumerFactory,
                                                          String topic,
                                                          int minMessageCount,
                                                          long pollTimeoutMillis) {

        try (AdminClient adminClient = AdminClient.create(admin.getConfigurationProperties())) {
            Set<String> topicToConsume = Set.of(topic);

            await().atMost(20, TimeUnit.SECONDS).pollInterval(Duration.ofSeconds(1)).until(() -> {

                boolean result = false;
                Collection<TopicListing> existingTopicList = adminClient.listTopics().listings().get();
                for (TopicListing existingTopic : existingTopicList) {
                    if (topicToConsume.contains(existingTopic.name())) {
                        result = true;
                    }
                }
                return result;
            });
        }

        List<ConsumerRecord<String, T>> consumerRecords = new ArrayList<>();
        try (Consumer<String, T> consumer = kafkaConsumerFactory.createConsumer()) {
            //try (final KafkaConsumer<String, CreditPolicyResponseAvro> consumer = createBookmarkEventKafkaConsumer(consumerGroupId)) {
            // assign the consumer to all the partitions of the topic
            var topicPartitions = consumer.partitionsFor(topic).stream()
                    .map(partitionInfo -> new TopicPartition(topic, partitionInfo.partition()))
                    .collect(Collectors.toList());
            consumer.assign(topicPartitions);
            var start = System.currentTimeMillis();
            while (true) {
                final ConsumerRecords<String, T> records = consumer
                        .poll(Duration.ofMillis(POLL_INTERVAL_MS));
                records.forEach(consumerRecords::add);
                if (consumerRecords.size() >= minMessageCount) {
                    break;
                }
                if (System.currentTimeMillis() - start > pollTimeoutMillis) {
                    throw new IllegalStateException(
                            String.format(
                                    "Timed out while waiting for %d messages from the %s. Only %d messages received so far.",
                                    minMessageCount, topic, consumerRecords.size()));
                }
            }
        }
        return consumerRecords;

    }


    private void createTestDatabase() throws Exception {
        Connection dbConnection = null;
        try {
            dbConnection = dataSource.getConnection();
            //create test database
            ScriptUtils.executeSqlScript(dbConnection,
                    new ClassPathResource("sql/create_public_schema.sql"));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (dbConnection != null) dbConnection.close();
            } catch (Exception e) {
            }
            ;
        }
    }

    private void cleanTestDatabase() throws Exception {
        Connection dbConnection = null;
        try {
            dbConnection = dataSource.getConnection();
            ScriptUtils.executeSqlScript(dbConnection,
                    new ClassPathResource("sql/clean_db_schema.sql"));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (dbConnection != null) dbConnection.close();
            } catch (Exception e) {
            }
        }
    }
}
