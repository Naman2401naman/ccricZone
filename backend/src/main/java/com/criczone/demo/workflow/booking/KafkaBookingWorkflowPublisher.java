package com.criczone.demo.workflow.booking;

import com.criczone.demo.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class KafkaBookingWorkflowPublisher implements BookingWorkflowPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaBookingWorkflowPublisher.class);

    private final KafkaTemplate<String, BookingWorkflowEvent> kafkaTemplate;
    private final AppProperties appProperties;

    public KafkaBookingWorkflowPublisher(KafkaTemplate<String, BookingWorkflowEvent> kafkaTemplate, AppProperties appProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.appProperties = appProperties;
    }

    @Override
    public void publish(BookingWorkflowEvent event) {
        kafkaTemplate.send(appProperties.getKafka().getBookingTopic(), event.getBookingId(), event);
        log.info("Published booking workflow event {} for booking {}", event.getEventType(), event.getBookingId());
    }
}
