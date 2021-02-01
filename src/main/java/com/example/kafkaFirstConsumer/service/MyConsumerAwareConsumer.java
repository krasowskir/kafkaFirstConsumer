package com.example.kafkaFirstConsumer.service;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.ConsumerAwareMessageListener;

public class MyConsumerAwareConsumer implements ConsumerAwareMessageListener<String,String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyConsumerAwareConsumer.class);

    @Override
    public void onMessage(ConsumerRecord consumerRecord, Consumer consumer) {
        LOGGER.info("received message {} on consumer {}", consumer.toString(),consumerRecord.toString());
    }

    @Override
    public void onMessage(ConsumerRecord data) {
        LOGGER.info("received message {} ",data.toString());
    }
}
