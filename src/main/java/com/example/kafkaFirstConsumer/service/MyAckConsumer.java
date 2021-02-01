package com.example.kafkaFirstConsumer.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

public class MyAckConsumer implements AcknowledgingMessageListener<String, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyAckConsumer.class);

    @Override
    public void onMessage(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
        System.out.println(consumerRecord.toString());
        LOGGER.info(consumerRecord.toString());
        acknowledgment.acknowledge();
    }

    @Override
    public void onMessage(ConsumerRecord<String, String> data) {
        LOGGER.info(data.toString());
        System.out.println(data.toString());
    }
}
