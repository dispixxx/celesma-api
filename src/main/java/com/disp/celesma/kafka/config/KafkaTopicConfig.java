package com.disp.celesma.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("kafka")
public class KafkaTopicConfig {

    public static final String TASK_EVENTS_TOPIC = "task-events";

    @Bean
    public NewTopic taskEventsTopic() {
        return TopicBuilder.name(TASK_EVENTS_TOPIC)
                .partitions(3)
                .replicas((short) 1)
                .build();
    }
}
