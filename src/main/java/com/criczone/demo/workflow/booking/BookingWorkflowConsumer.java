package com.criczone.demo.workflow.booking;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class BookingWorkflowConsumer {

    private final BookingWorkflowProjectionService projectionService;

    public BookingWorkflowConsumer(BookingWorkflowProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    @KafkaListener(
        topics = "${app.kafka.booking-topic}",
        groupId = "${app.kafka.consumer-group-id}",
        containerFactory = "bookingWorkflowKafkaListenerContainerFactory")
    public void consume(BookingWorkflowEvent event) {
        projectionService.record(event);
    }
}
