package com.criczone.demo.domain;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("bookings")
public class BookingDocument {
    @Id
    private String id;
    private String turf;
    private String user;
    private String date;
    private String startTime;
    private String endTime;
    private Double totalPrice;
    private Double slotHours = 1d;
    private String status = "booked";
    private Instant cancelledAt;
    private Map<String, Object> billing = new LinkedHashMap<>();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTurf() { return turf; }
    public void setTurf(String turf) { this.turf = turf; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public Double getSlotHours() { return slotHours; }
    public void setSlotHours(Double slotHours) { this.slotHours = slotHours; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
    public Map<String, Object> getBilling() { return billing; }
    public void setBilling(Map<String, Object> billing) { this.billing = billing; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
