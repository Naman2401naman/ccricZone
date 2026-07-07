package com.criczone.demo.workflow.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpBookingWorkflowPublisher implements BookingWorkflowPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpBookingWorkflowPublisher.class);

    @Override
    public void publish(BookingWorkflowEvent event) {
        log.info("Kafka disabled. Skipping booking workflow event {} for booking {}", event.getEventType(), event.getBookingId());
    }
}
