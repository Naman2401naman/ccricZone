package com.criczone.demo.workflow.booking;

public interface BookingWorkflowPublisher {
    void publish(BookingWorkflowEvent event);
}
