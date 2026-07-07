package com.criczone.demo.api;

import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.dto.ApiRequests.CreateBookingRequest;
import com.criczone.demo.dto.ApiRequests.UpdatePaymentRequest;
import com.criczone.demo.dto.RequestMaps;
import com.criczone.demo.service.BookingService;
import com.criczone.demo.support.RequestTracingFilter;
import java.util.Map;
import javax.validation.Valid;
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

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public Map<String, Object> create(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      @RequestAttribute(value = RequestTracingFilter.REQUEST_ID_ATTRIBUTE, required = false) String requestId,
                                      @Valid @RequestBody CreateBookingRequest request) {
        return bookingService.create(currentUser, requestId, RequestMaps.toMap(request));
    }

    @GetMapping("/mybookings")
    public Map<String, Object> myBookings(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                          @RequestParam(required = false) String from,
                                          @RequestParam(required = false) String to) {
        return bookingService.myBookings(currentUser, from, to);
    }

    @GetMapping("/mybookings/report.csv")
    public ResponseEntity<String> myBookingsCsv(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        return bookingService.myBookingsCsv(currentUser);
    }

    @PutMapping("/{id}/cancel")
    public Map<String, Object> cancel(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      @RequestAttribute(value = RequestTracingFilter.REQUEST_ID_ATTRIBUTE, required = false) String requestId,
                                      @PathVariable String id) {
        return bookingService.cancel(currentUser, requestId, id);
    }

    @GetMapping
    public Map<String, Object> all(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        return bookingService.all(currentUser);
    }

    @GetMapping("/billing/summary")
    public Map<String, Object> billingSummary(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        return bookingService.billingSummary(currentUser);
    }

    @GetMapping("/billing/report.csv")
    public ResponseEntity<String> billingReport(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        return bookingService.billingReport(currentUser);
    }

    @PutMapping("/{id}/payment")
    public Map<String, Object> updatePayment(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                             @RequestAttribute(value = RequestTracingFilter.REQUEST_ID_ATTRIBUTE, required = false) String requestId,
                                             @PathVariable String id,
                                             @Valid @RequestBody UpdatePaymentRequest request) {
        return bookingService.updatePayment(currentUser, requestId, id, RequestMaps.toMap(request));
    }

    @GetMapping({"/{id}/workflow", "/{id}/history"})
    public Map<String, Object> workflow(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                        @PathVariable String id) {
        return bookingService.workflow(currentUser, id);
    }

    @GetMapping("/workflow/summary")
    public Map<String, Object> workflowSummary(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser) {
        return bookingService.workflowSummary(currentUser);
    }
}
