package com.criczone.demo.api;

import com.criczone.demo.config.CacheNames;
import com.criczone.demo.domain.TurfDocument;
import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.repo.TurfRepository;
import com.criczone.demo.support.ApiException;
import com.criczone.demo.support.ApiSupport;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/turfs")
public class TurfController {

    private final TurfRepository turfRepository;

    public TurfController(TurfRepository turfRepository) {
        this.turfRepository = turfRepository;
    }

    @PostMapping("/add")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.TURF_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TURF_NEARBY, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TURF_BY_ID, allEntries = true)
    })
    public Map<String, Object> add(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                   @RequestBody Map<String, Object> request) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        ApiSupport.requireRole(user, "admin", "turf_owner");
        TurfDocument turf = new TurfDocument();
        turf.setTurfName(ApiSupport.trim(request.get("turfName")));
        turf.setOwnerId(user.getId());
        turf.setLocation(castMap(request.get("location")));
        turf.setSportTypes(castStringList(request.get("sportTypes")));
        turf.setTurfSize(castMap(request.get("turfSize")));
        turf.setSurfaceType(ApiSupport.trim(request.get("surfaceType")).toLowerCase());
        turf.setAmenities(castMap(request.get("amenities")));
        turf.setImages(castStringList(request.get("images")));
        turf.setBasePricingPerSlot(ApiSupport.safeDouble(request.get("basePricingPerSlot")));
        turf.setCreatedAt(Instant.now());
        turf.setUpdatedAt(Instant.now());
        turfRepository.save(turf);
        return Map.of("success", true, "message", "Turf added successfully", "data", turf);
    }

    @GetMapping("/all")
    @Cacheable(CacheNames.TURF_LIST)
    public Map<String, Object> all() {
        List<TurfDocument> turfs = turfRepository.findByIsActiveTrue().stream()
            .sorted(Comparator.comparing(TurfDocument::getCreatedAt).reversed())
            .collect(Collectors.toList());
        return Map.of("success", true, "count", turfs.size(), "data", turfs);
    }

    @GetMapping("/owned")
    public Map<String, Object> owned(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        ApiSupport.requireRole(user, "admin", "turf_owner");
        List<TurfDocument> turfs = "admin".equalsIgnoreCase(user.getRole()) ? turfRepository.findAll() : turfRepository.findByOwnerId(user.getId());
        return Map.of("success", true, "count", turfs.size(), "data", turfs);
    }

    @PostMapping("/nearby")
    @Cacheable(cacheNames = CacheNames.TURF_NEARBY, key = "T(java.lang.String).format('%s:%s:%s', #request['latitude'], #request['longitude'], #request['maxDistance'])")
    public Map<String, Object> nearby(@RequestBody Map<String, Object> request) {
        double latitude = ApiSupport.safeDouble(request.get("latitude"));
        double longitude = ApiSupport.safeDouble(request.get("longitude"));
        double maxDistance = request.get("maxDistance") == null ? 5000 : ApiSupport.safeDouble(request.get("maxDistance"));
        List<TurfDocument> turfs = turfRepository.findByIsActiveTrue().stream()
            .filter(turf -> distanceMeters(longitude, latitude, turf) <= maxDistance)
            .collect(Collectors.toList());
        return Map.of("success", true, "count", turfs.size(), "data", turfs);
    }

    @GetMapping("/{id}")
    @Cacheable(cacheNames = CacheNames.TURF_BY_ID, key = "#id")
    public Map<String, Object> one(@PathVariable String id) {
        TurfDocument turf = turfRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Turf not found"));
        return Map.of("success", true, "data", turf);
    }

    @PutMapping("/{id}")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.TURF_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TURF_NEARBY, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TURF_BY_ID, key = "#id")
    })
    public Map<String, Object> update(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      @PathVariable String id,
                                      @RequestBody Map<String, Object> request) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        ApiSupport.requireRole(user, "admin", "turf_owner");
        TurfDocument turf = turfRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Turf not found"));
        if (!Objects.equals(turf.getOwnerId(), user.getId()) && !"admin".equalsIgnoreCase(user.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized to update this turf");
        }
        if (request.get("turfName") != null) turf.setTurfName(ApiSupport.trim(request.get("turfName")));
        if (request.get("location") instanceof Map<?, ?>) turf.setLocation(castMap(request.get("location")));
        if (request.get("sportTypes") instanceof List<?>) turf.setSportTypes(castStringList(request.get("sportTypes")));
        if (request.get("turfSize") instanceof Map<?, ?>) turf.setTurfSize(castMap(request.get("turfSize")));
        if (request.get("surfaceType") != null) turf.setSurfaceType(ApiSupport.trim(request.get("surfaceType")));
        if (request.get("amenities") instanceof Map<?, ?>) turf.setAmenities(castMap(request.get("amenities")));
        if (request.get("images") instanceof List<?>) turf.setImages(castStringList(request.get("images")));
        if (request.get("basePricingPerSlot") != null) turf.setBasePricingPerSlot(ApiSupport.safeDouble(request.get("basePricingPerSlot")));
        turf.setUpdatedAt(Instant.now());
        turfRepository.save(turf);
        return Map.of("success", true, "message", "Turf updated successfully", "data", turf);
    }

    @DeleteMapping("/{id}")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.TURF_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TURF_NEARBY, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.TURF_BY_ID, key = "#id")
    })
    public Map<String, Object> delete(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      @PathVariable String id) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        ApiSupport.requireRole(user, "admin", "turf_owner");
        TurfDocument turf = turfRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Turf not found"));
        if (!Objects.equals(turf.getOwnerId(), user.getId()) && !"admin".equalsIgnoreCase(user.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized to delete this turf");
        }
        turfRepository.delete(turf);
        return Map.of("success", true, "message", "Turf deleted successfully");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        if (value instanceof Map<?, ?>) {
            return (Map<String, Object>) value;
        }
        return new java.util.LinkedHashMap<String, Object>();
    }

    private List<String> castStringList(Object value) {
        if (!(value instanceof List<?>)) return new java.util.ArrayList<String>();
        List<?> list = (List<?>) value;
        return list.stream().map(String::valueOf).collect(Collectors.toList());
    }

    private double distanceMeters(double lon, double lat, TurfDocument turf) {
        Object coordinatesObj = ((Map<?, ?>) turf.getLocation().getOrDefault("coordinates", Map.of())).get("coordinates");
        if (!(coordinatesObj instanceof List<?>)) return Double.MAX_VALUE;
        List<?> coords = (List<?>) coordinatesObj;
        if (coords.size() < 2) return Double.MAX_VALUE;
        double turfLon = Double.parseDouble(String.valueOf(coords.get(0)));
        double turfLat = Double.parseDouble(String.valueOf(coords.get(1)));
        double earthRadius = 6371000;
        double dLat = Math.toRadians(turfLat - lat);
        double dLon = Math.toRadians(turfLon - lon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(turfLat)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
