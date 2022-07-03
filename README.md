# Device Status Notifier

### Requirements

We have system that throws network devices alerts by sending data into a kafka bus,
the purpose of this task is to write a small (or a small set) of microservices that:

get the tasks from a defined kafka topic and persist it into a mongo db
identify the device that was alerted and update his status on the sql db
push notification to end users
In order to do push notifications the only requirement is to push a message with the alert into the kafka topic named "push notifications" and request a POST call to an endpoint on "statuschange.com/devices/<id>" sending the payload that includes the relevant new status.

mongo db document structure can be assumed to match exactly the message you're getting on the topic, so you can simply persist it without any additional change.

the alert event provides this data:

{

"id": integer,

"status":string //it might be down, up or alerted

}



on the sql db you can assume there is a devices table that stores device name, id and status.

Notifications should be sent on status change scenarios (up->alerted, alerted->down|up, down->alerted)


Things we care about:

testing (be able to mock)
code quality
scalability and reliability


### Build and run the application locally

1) Run a docker Postgres database for dev:
```
$docker-compose -f ./docker/docker-compose-postgres.yml up
```
2) Run a docker Mongo database for dev:
```
$docker-compose -f ./docker/docker-compose-mongo-dev.yml up
```
3) Run a docker Kafka server for dev:
```
$docker-compose -f ./docker/docker-compose-kafka.yml up
```

4) Run a docker Wiremock to simulate external notify endpoint "statuschange.com/devices/<id>" for dev:
```
$docker-compose -f ./app/src/intTest/resources/docker/docker-compose-wiremock-test.yml up
```


5) Insert a device in Postgres db

```
INSERT INTO "public"."devices" ("name", "status") VALUES ('router', 'down');
```

6) Run the app locally
```sh
./gradlew bootRun
```



7) Send device status events:
```sh
curl --location --request POST 'http://localhost:8080/api/fake/devicestatus' \
--header 'Content-Type: application/json' \
--data-raw '{
    "device_id" : "1",
    "status" : "alerted"
}'
```

For scalability, the number of consumers can be set in property:

```
kafka.concurrency.deviceStatusConsumer
```

### Build the project and run unit and integration tests with real testcontainers:

```sh
ORG_GRADLE_PROJECT_intTest=true SPRING_PROFILES_ACTIVE=intTest ./gradlew clean build test --info --stacktrace
```
Go to:

```sh
app/build/reports/tests/integrationTest/index.html
```

#### DeviceStatusIntegrationTest

This test exercises this scenario:
- "down -> alerted - should generate push notification"


#### NotifyEndpointHttpConnectionTest

There is a retry strategy for the external endpoint: NotifyEndpointRetryStrategyConfig
This test exercises the external endpoint errors:

- Test notify endpoint connect timed out
- Test notify endpoint Connection refused
- Test notify endpoint Read timed out
