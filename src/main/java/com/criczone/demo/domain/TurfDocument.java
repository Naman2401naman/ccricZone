package com.criczone.demo.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("turfs")
public class TurfDocument {
    @Id
    private String id;
    private String turfName;
    private String ownerId;
    private Map<String, Object> location = new LinkedHashMap<>();
    private List<String> sportTypes = new ArrayList<>();
    private Map<String, Object> turfSize = new LinkedHashMap<>();
    private String surfaceType;
    private Map<String, Object> amenities = new LinkedHashMap<>();
    private List<String> images = new ArrayList<>();
    private Double basePricingPerSlot;
    private Double rating = 0d;
    private Integer totalReviews = 0;
    private Boolean isActive = true;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTurfName() { return turfName; }
    public void setTurfName(String turfName) { this.turfName = turfName; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public Map<String, Object> getLocation() { return location; }
    public void setLocation(Map<String, Object> location) { this.location = location; }
    public List<String> getSportTypes() { return sportTypes; }
    public void setSportTypes(List<String> sportTypes) { this.sportTypes = sportTypes; }
    public Map<String, Object> getTurfSize() { return turfSize; }
    public void setTurfSize(Map<String, Object> turfSize) { this.turfSize = turfSize; }
    public String getSurfaceType() { return surfaceType; }
    public void setSurfaceType(String surfaceType) { this.surfaceType = surfaceType; }
    public Map<String, Object> getAmenities() { return amenities; }
    public void setAmenities(Map<String, Object> amenities) { this.amenities = amenities; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public Double getBasePricingPerSlot() { return basePricingPerSlot; }
    public void setBasePricingPerSlot(Double basePricingPerSlot) { this.basePricingPerSlot = basePricingPerSlot; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
