package com.criczone.demo.domain;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("booking_workflow_events")
public class BookingWorkflowEventDocument {
    @Id
    private String id;
    private String eventId;
    private String bookingId;
    private String turfId;
    private String userId;
    private String ownerId;
    private String eventType;
    private String bookingStatus;
    private String paymentStatus;
    private Double amount;
    private String triggeredByUserId;
    private String source;
    private String requestId;
    private Map<String, Object> metadata = new LinkedHashMap<>();
    private Instant occurredAt;
    private Instant consumedAt = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
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
    public Instant getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Instant consumedAt) { this.consumedAt = consumedAt; }
}
