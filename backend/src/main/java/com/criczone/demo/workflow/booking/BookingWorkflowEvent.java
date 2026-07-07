package com.criczone.demo.workflow.booking;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class BookingWorkflowEvent {

    private String eventId;
    private String bookingId;
    private String turfId;
    private String userId;
    private String ownerId;
    private BookingWorkflowEventType eventType;
    private String bookingStatus;
    private String paymentStatus;
    private Double amount;
    private String triggeredByUserId;
    private String source;
    private String requestId;
    private Map<String, Object> metadata = new LinkedHashMap<>();
    private Instant occurredAt;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getTurfId() { return turfId; }
    public void setTurfId(String turfId) { this.turfId = turfId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public BookingWorkflowEventType getEventType() { return eventType; }
    public void setEventType(BookingWorkflowEventType eventType) { this.eventType = eventType; }
    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getTriggeredByUserId() { return triggeredByUserId; }
    public void setTriggeredByUserId(String triggeredByUserId) { this.triggeredByUserId = triggeredByUserId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
