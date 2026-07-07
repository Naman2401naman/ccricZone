package com.criczone.demo.service;

import com.criczone.demo.domain.BookingDocument;
import com.criczone.demo.domain.TurfDocument;
import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.repo.BookingRepository;
import com.criczone.demo.repo.BookingWorkflowEventRepository;
import com.criczone.demo.repo.BookingWorkflowSnapshotRepository;
import com.criczone.demo.repo.TurfRepository;
import com.criczone.demo.support.ApiException;
import com.criczone.demo.workflow.booking.BookingWorkflowPublisher;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingServiceTest {

    @Test
    void createRejectsOverlappingBookedSlot() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        TurfRepository turfRepository = mock(TurfRepository.class);
        BookingWorkflowPublisher workflowPublisher = mock(BookingWorkflowPublisher.class);
        BookingService bookingService = new BookingService(
            bookingRepository,
            turfRepository,
            workflowPublisher,
            mock(BookingWorkflowEventRepository.class),
            mock(BookingWorkflowSnapshotRepository.class));

        UserDocument user = new UserDocument();
        user.setId("user-1");
        TurfDocument turf = new TurfDocument();
        turf.setId("turf-1");
        turf.setBasePricingPerSlot(1000d);
        BookingDocument existing = new BookingDocument();
        existing.setStartTime("10:30");
        existing.setEndTime("11:30");

        when(turfRepository.findById("turf-1")).thenReturn(Optional.of(turf));
        when(bookingRepository.findByTurfAndDateAndStatus("turf-1", "2026-07-08", "booked"))
            .thenReturn(List.of(existing));

        ApiException error = assertThrows(ApiException.class, () -> bookingService.create(user, "req-1", Map.of(
            "turfId", "turf-1",
            "date", "2026-07-08",
            "startTime", "10:00",
            "endTime", "11:00")));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        verify(bookingRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(workflowPublisher, never()).publish(org.mockito.ArgumentMatchers.any());
    }
}
