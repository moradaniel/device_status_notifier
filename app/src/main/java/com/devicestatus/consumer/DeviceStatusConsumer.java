package com.devicestatus.consumer;

import com.devicestatus.domain.DeviceStatusEvent;
import com.devicestatus.domain.DeviceStatusEventAvro;
import com.devicestatus.service.DeviceStatusService;
import com.devicestatus.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceStatusConsumer {
    private final DeviceStatusService deviceStatusService;

    @KafkaListener(topics = "${kafka.topics.device-status-events}",
            groupId = "${kafka.group-ids.device-status.group}",
            id = "${kafka.ids.device-status}",
            concurrency = "${kafka.concurrency.deviceStatusConsumer}"
    )
    public void consume(ConsumerRecord<String, DeviceStatusEventAvro> record, Acknowledgment acknowledgment) throws Exception{
        log.info("consumed avro from topic: "+record.topic()+" deviceId={} deviceStatus={}", record.value().getDeviceId(), record.value());

        try {
            deviceStatusService.process(DeviceStatusEvent.of(record.value()));
        }
        catch(Exception ex){
            log.error(Utils.makeExceptionLogEntry(ex, ex.getMessage(), record.value().getDeviceId()));
        }finally {
            acknowledgment.acknowledge();
        }

    }



}
