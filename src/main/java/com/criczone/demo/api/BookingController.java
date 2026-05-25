package com.criczone.demo.api;

import com.criczone.demo.domain.BookingDocument;
import com.criczone.demo.domain.BookingWorkflowEventDocument;
import com.criczone.demo.domain.BookingWorkflowSnapshotDocument;
import com.criczone.demo.domain.TurfDocument;
import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.repo.BookingRepository;
import com.criczone.demo.repo.BookingWorkflowEventRepository;
import com.criczone.demo.repo.BookingWorkflowSnapshotRepository;
import com.criczone.demo.repo.TurfRepository;
import com.criczone.demo.support.ApiException;
import com.criczone.demo.support.ApiSupport;
import com.criczone.demo.support.RequestTracingFilter;
import com.criczone.demo.workflow.booking.BookingWorkflowEvent;
import com.criczone.demo.workflow.booking.BookingWorkflowEventType;
import com.criczone.demo.workflow.booking.BookingWorkflowPublisher;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final TurfRepository turfRepository;
    private final BookingWorkflowPublisher bookingWorkflowPublisher;
    private final BookingWorkflowEventRepository bookingWorkflowEventRepository;
    private final BookingWorkflowSnapshotRepository bookingWorkflowSnapshotRepository;

    public BookingController(BookingRepository bookingRepository,
                             TurfRepository turfRepository,
                             BookingWorkflowPublisher bookingWorkflowPublisher,
                             BookingWorkflowEventRepository bookingWorkflowEventRepository,
                             BookingWorkflowSnapshotRepository bookingWorkflowSnapshotRepository) {
        this.bookingRepository = bookingRepository;
        this.turfRepository = turfRepository;
        this.bookingWorkflowPublisher = bookingWorkflowPublisher;
        this.bookingWorkflowEventRepository = bookingWorkflowEventRepository;
        this.bookingWorkflowSnapshotRepository = bookingWorkflowSnapshotRepository;
    }

    @PostMapping
    public Map<String, Object> create(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      @RequestAttribute(value = RequestTracingFilter.REQUEST_ID_ATTRIBUTE, required = false) String requestId,
                                      @RequestBody Map<String, Object> request) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        String turfId = ApiSupport.trim(request.get("turfId"));
        TurfDocument turf = turfRepository.findById(turfId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Turf not found"));
        int start = ApiSupport.parseTimeToMinutes(ApiSupport.trim(request.get("startTime")));
        int end = ApiSupport.parseTimeToMinutes(ApiSupport.trim(request.get("endTime")));
        if (end <= start) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "endTime must be greater than startTime");
        }
        for (BookingDocument booking : bookingRepository.findByTurfAndDateAndStatus(turfId, ApiSupport.trim(request.get("date")), "booked")) {
            int existingStart = ApiSupport.parseTimeToMinutes(booking.getStartTime());
            int existingEnd = ApiSupport.parseTimeToMinutes(booking.getEndTime());
            if (start < existingEnd && existingStart < end) {
                throw new ApiException(HttpStatus.CONFLICT, "Selected time slot is already booked");
            }
        }
        double hours = (end - start) / 60.0;
        BookingDocument booking = new BookingDocument();
        booking.setTurf(turfId);
        booking.setUser(user.getId());
        booking.setDate(ApiSupport.trim(request.get("date")));
        booking.setStartTime(ApiSupport.trim(request.get("startTime")));
        booking.setEndTime(ApiSupport.trim(request.get("endTime")));
        booking.setSlotHours(hours);
        booking.setTotalPrice(Math.round(turf.getBasePricingPerSlot() * hours * 100.0) / 100.0);
        booking.setBilling(new LinkedHashMap<>(Map.of("invoiceNumber", ApiSupport.invoiceNumber(), "currency", "INR", "paymentStatus", "pending", "paymentMethod", null, "paymentReference", "", "paidAt", null)));
        booking.setCreatedAt(Instant.now());
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);
        publishWorkflowEvent(
            BookingWorkflowEventType.BOOKING_CREATED,
            booking,
            turf,
            user.getId(),
            requestId,
            "/api/bookings",
            ApiSupport.mapOf(
                "date", booking.getDate(),
                "startTime", booking.getStartTime(),
                "endTime", booking.getEndTime(),
                "invoiceNumber", booking.getBilling().get("invoiceNumber")));
        return Map.of("success", true, "message", "Booking created successfully", "booking", booking);
    }

    @GetMapping("/mybookings")
    public Map<String, Object> myBookings(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                          @RequestParam(required = false) String from,
                                          @RequestParam(required = false) String to) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        List<BookingDocument> bookings = bookingRepository.findByUserOrderByCreatedAtDesc(user.getId());
        return Map.of("success", true, "bookings", bookings);
    }

    @GetMapping("/mybookings/report.csv")
    public ResponseEntity<String> myBookingsCsv(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        return csvResponse("my-bookings.csv", bookingRepository.findByUserOrderByCreatedAtDesc(user.getId()));
    }

    @PutMapping("/{id}/cancel")
    public Map<String, Object> cancel(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      @RequestAttribute(value = RequestTracingFilter.REQUEST_ID_ATTRIBUTE, required = false) String requestId,
                                      @PathVariable String id) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        BookingDocument booking = bookingRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        TurfDocument turf = turfRepository.findById(booking.getTurf()).orElse(null);
        boolean canManage = Objects.equals(booking.getUser(), user.getId()) || "admin".equalsIgnoreCase(user.getRole()) || (turf != null && Objects.equals(turf.getOwnerId(), user.getId()));
        if (!canManage) throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
        booking.setStatus("cancelled");
        booking.setCancelledAt(Instant.now());
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);
        publishWorkflowEvent(
            BookingWorkflowEventType.BOOKING_CANCELLED,
            booking,
            turf,
            user.getId(),
            requestId,
            "/api/bookings/" + id + "/cancel",
            ApiSupport.mapOf(
                "cancelledAt", booking.getCancelledAt() == null ? null : booking.getCancelledAt().toString(),
                "managedByRole", user.getRole()));
        return Map.of("success", true, "message", "Booking cancelled successfully", "booking", booking);
    }

    @GetMapping
    public Map<String, Object> all(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        ApiSupport.requireRole(user, "admin", "turf_owner");
        List<BookingDocument> bookings = filterManagedBookings(user);
        return Map.of("success", true, "count", bookings.size(), "bookings", bookings);
    }

    @GetMapping("/billing/summary")
    public Map<String, Object> billingSummary(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        ApiSupport.requireRole(user, "admin", "turf_owner");
        List<BookingDocument> bookings = filterManagedBookings(user);
        double totalPaid = bookings.stream().filter(booking -> Objects.equals(String.valueOf(booking.getBilling().get("paymentStatus")), "paid")).mapToDouble(BookingDocument::getTotalPrice).sum();
        double totalPending = bookings.stream().filter(booking -> !Objects.equals(String.valueOf(booking.getBilling().get("paymentStatus")), "paid")).mapToDouble(BookingDocument::getTotalPrice).sum();
        return Map.of("success", true, "summary", Map.of("totalBookings", bookings.size(), "totalPaid", totalPaid, "totalPending", totalPending), "bookings", bookings);
    }

    @GetMapping("/billing/report.csv")
    public ResponseEntity<String> billingReport(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        ApiSupport.requireRole(user, "admin", "turf_owner");
        return csvResponse("billing-report.csv", filterManagedBookings(user));
    }

    @PutMapping("/{id}/payment")
    public Map<String, Object> updatePayment(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                             @RequestAttribute(value = RequestTracingFilter.REQUEST_ID_ATTRIBUTE, required = false) String requestId,
                                             @PathVariable String id,
                                             @RequestBody Map<String, Object> request) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        ApiSupport.requireRole(user, "admin", "turf_owner");
        BookingDocument booking = bookingRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        booking.getBilling().put("paymentStatus", ApiSupport.trim(request.get("paymentStatus")));
        booking.getBilling().put("paymentMethod", request.get("paymentMethod"));
        if (request.get("paymentReference") != null) {
            booking.getBilling().put("paymentReference", ApiSupport.trim(request.get("paymentReference")));
        }
        if ("paid".equalsIgnoreCase(ApiSupport.trim(request.get("paymentStatus")))) {
            booking.getBilling().put("paidAt", Instant.now().toString());
        }
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);
        TurfDocument turf = turfRepository.findById(booking.getTurf()).orElse(null);
        publishWorkflowEvent(
            BookingWorkflowEventType.BOOKING_PAYMENT_UPDATED,
            booking,
            turf,
            user.getId(),
            requestId,
            "/api/bookings/" + id + "/payment",
            ApiSupport.mapOf(
                "paymentMethod", booking.getBilling().get("paymentMethod"),
                "paymentReference", booking.getBilling().get("paymentReference"),
                "paidAt", booking.getBilling().get("paidAt")));
        return Map.of("success", true, "message", "Payment status updated successfully", "booking", booking);
    }

    @GetMapping("/{id}/workflow")
    public Map<String, Object> workflow(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                        @PathVariable String id) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        BookingDocument booking = bookingRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        TurfDocument turf = turfRepository.findById(booking.getTurf()).orElse(null);
        ensureBookingAccess(user, booking, turf);
        List<BookingWorkflowEventDocument> events = bookingWorkflowEventRepository.findByBookingIdOrderByOccurredAtAsc(id);
        BookingWorkflowSnapshotDocument snapshot = bookingWorkflowSnapshotRepository.findById(id).orElse(null);
        return Map.of(
            "success", true,
            "bookingId", id,
            "workflowEnabled", !events.isEmpty() || snapshot != null,
            "timelineCount", events.size(),
            "snapshot", snapshot,
            "timeline", events);
    }

    @GetMapping("/workflow/summary")
    public Map<String, Object> workflowSummary(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        List<BookingWorkflowSnapshotDocument> snapshots = visibleSnapshots(user);
        Map<String, Long> byStatus = snapshots.stream()
            .collect(Collectors.groupingBy(snapshot -> Optional.ofNullable(snapshot.getCurrentStatus()).orElse("unknown"), LinkedHashMap::new, Collectors.counting()));
        Map<String, Long> byPaymentStatus = snapshots.stream()
            .collect(Collectors.groupingBy(snapshot -> Optional.ofNullable(snapshot.getPaymentStatus()).orElse("unknown"), LinkedHashMap::new, Collectors.counting()));
        return Map.of(
            "success", true,
            "count", snapshots.size(),
            "summary", Map.of(
                "byStatus", byStatus,
                "byPaymentStatus", byPaymentStatus),
            "workflows", snapshots);
    }

    private List<BookingDocument> filterManagedBookings(UserDocument user) {
        List<BookingDocument> all = bookingRepository.findAll().stream().sorted(Comparator.comparing(BookingDocument::getCreatedAt).reversed()).collect(Collectors.toList());
        if ("admin".equalsIgnoreCase(user.getRole())) {
            return all;
        }
        List<String> managedTurfIds = turfRepository.findByOwnerId(user.getId()).stream().map(TurfDocument::getId).collect(Collectors.toList());
        return all.stream().filter(booking -> managedTurfIds.contains(booking.getTurf())).collect(Collectors.toList());
    }

    private ResponseEntity<String> csvResponse(String filename, List<BookingDocument> bookings) {
        StringBuilder builder = new StringBuilder("BookingId,Date,StartTime,EndTime,Status,Amount,Invoice,PaymentStatus\n");
        for (BookingDocument booking : bookings) {
            builder.append(booking.getId()).append(",")
                .append(booking.getDate()).append(",")
                .append(booking.getStartTime()).append(",")
                .append(booking.getEndTime()).append(",")
                .append(booking.getStatus()).append(",")
                .append(booking.getTotalPrice()).append(",")
                .append(booking.getBilling().get("invoiceNumber")).append(",")
                .append(booking.getBilling().get("paymentStatus")).append("\n");
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .body(builder.toString());
    }

    private void publishWorkflowEvent(BookingWorkflowEventType eventType,
                                      BookingDocument booking,
                                      TurfDocument turf,
                                      String triggeredByUserId,
                                      String requestId,
                                      String source,
                                      Map<String, Object> metadata) {
        BookingWorkflowEvent event = new BookingWorkflowEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setBookingId(booking.getId());
        event.setTurfId(booking.getTurf());
        event.setUserId(booking.getUser());
        event.setOwnerId(turf == null ? null : turf.getOwnerId());
        event.setEventType(eventType);
        event.setBookingStatus(booking.getStatus());
        event.setPaymentStatus(String.valueOf(booking.getBilling().getOrDefault("paymentStatus", "unknown")));
        event.setAmount(booking.getTotalPrice());
        event.setTriggeredByUserId(triggeredByUserId);
        event.setRequestId(requestId);
        event.setSource(source);
        event.setMetadata(metadata == null ? Map.of() : metadata);
        event.setOccurredAt(Instant.now());
        bookingWorkflowPublisher.publish(event);
    }

    private void ensureBookingAccess(UserDocument user, BookingDocument booking, TurfDocument turf) {
        boolean allowed = Objects.equals(booking.getUser(), user.getId())
            || "admin".equalsIgnoreCase(user.getRole())
            || (turf != null && Objects.equals(turf.getOwnerId(), user.getId()));
        if (!allowed) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
        }
    }

    private List<BookingWorkflowSnapshotDocument> visibleSnapshots(UserDocument user) {
        if ("admin".equalsIgnoreCase(user.getRole())) {
            return bookingWorkflowSnapshotRepository.findAll().stream()
                .sorted(Comparator.comparing(BookingWorkflowSnapshotDocument::getUpdatedAt).reversed())
                .collect(Collectors.toList());
        }
        if ("turf_owner".equalsIgnoreCase(user.getRole())) {
            return bookingWorkflowSnapshotRepository.findByOwnerIdOrderByUpdatedAtDesc(user.getId());
        }
        return bookingWorkflowSnapshotRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
    }
}
