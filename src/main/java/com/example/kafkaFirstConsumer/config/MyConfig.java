package com.example.kafkaFirstConsumer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class MyConfig {

    @Bean
    public NewTopic topicExample() {
        return TopicBuilder.name("test-Topic")
                .partitions(2)
                .replicas(1)
                .build();
    }
}
