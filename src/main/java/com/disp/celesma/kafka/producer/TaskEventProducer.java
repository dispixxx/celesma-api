package com.disp.celesma.kafka.producer;

import com.disp.celesma.kafka.config.KafkaTopicConfig;
import com.disp.celesma.kafka.dto.TaskEventDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendTaskCreated(TaskEventDto event) {
        send(event, "TASK_CREATED");
    }

    public void sendTaskStatusChanged(TaskEventDto event) {
        send(event, "TASK_STATUS_CHANGED");
    }

    private void send(TaskEventDto event, String eventType) {
        try {
            event.setEventType(eventType);
            event.setOccurredAt(java.time.LocalDateTime.now());
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopicConfig.TASK_EVENTS_TOPIC, event.getTaskId().toString(), message);
            log.info("Sent event to Kafka topic {}: {}", KafkaTopicConfig.TASK_EVENTS_TOPIC, message);
        } catch (JsonProcessingException e) {
            log.error("Error serializing task event to JSON", e);
        }
    }
}
