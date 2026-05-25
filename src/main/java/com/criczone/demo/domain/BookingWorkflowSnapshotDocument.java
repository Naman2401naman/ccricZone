package com.criczone.demo.domain;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("booking_workflow_snapshots")
public class BookingWorkflowSnapshotDocument {
    @Id
    private String bookingId;
    private String turfId;
    private String userId;
    private String ownerId;
    private String currentStatus;
    private String paymentStatus;
    private String lastEventType;
    private Double totalAmount;
    private Integer eventCount = 0;
    private String lastRequestId;
    private Map<String, Object> latestMetadata = new LinkedHashMap<>();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private Instant lastEventAt = Instant.now();

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getTurfId() { return turfId; }
    public void setTurfId(String turfId) { this.turfId = turfId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getLastEventType() { return lastEventType; }
    public void setLastEventType(String lastEventType) { this.lastEventType = lastEventType; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public Integer getEventCount() { return eventCount; }
    public void setEventCount(Integer eventCount) { this.eventCount = eventCount; }
    public String getLastRequestId() { return lastRequestId; }
    public void setLastRequestId(String lastRequestId) { this.lastRequestId = lastRequestId; }
    public Map<String, Object> getLatestMetadata() { return latestMetadata; }
    public void setLatestMetadata(Map<String, Object> latestMetadata) { this.latestMetadata = latestMetadata; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getLastEventAt() { return lastEventAt; }
    public void setLastEventAt(Instant lastEventAt) { this.lastEventAt = lastEventAt; }
}
