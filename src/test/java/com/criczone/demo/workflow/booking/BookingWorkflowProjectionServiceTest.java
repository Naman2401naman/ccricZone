package com.criczone.demo.workflow.booking;

import com.criczone.demo.domain.BookingWorkflowEventDocument;
import com.criczone.demo.domain.BookingWorkflowSnapshotDocument;
import com.criczone.demo.repo.BookingWorkflowEventRepository;
import com.criczone.demo.repo.BookingWorkflowSnapshotRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingWorkflowProjectionServiceTest {

    @Mock
    private BookingWorkflowEventRepository eventRepository;

    @Mock
    private BookingWorkflowSnapshotRepository snapshotRepository;

    @InjectMocks
    private BookingWorkflowProjectionService projectionService;

    @Test
    void recordCreatesEventAndSnapshot() {
        BookingWorkflowEvent event = new BookingWorkflowEvent();
        event.setEventId("evt-1");
        event.setBookingId("booking-1");
        event.setTurfId("turf-1");
        event.setUserId("user-1");
        event.setOwnerId("owner-1");
        event.setEventType(BookingWorkflowEventType.BOOKING_CREATED);
        event.setBookingStatus("booked");
        event.setPaymentStatus("pending");
        event.setAmount(1200.0);
        event.setTriggeredByUserId("user-1");
        event.setSource("/api/bookings");
        event.setRequestId("req-1");
        event.setOccurredAt(Instant.parse("2026-04-26T06:30:00Z"));

        when(eventRepository.existsByEventId("evt-1")).thenReturn(false);
        when(snapshotRepository.findById("booking-1")).thenReturn(Optional.empty());

        projectionService.record(event);

        ArgumentCaptor<BookingWorkflowEventDocument> eventCaptor = ArgumentCaptor.forClass(BookingWorkflowEventDocument.class);
        verify(eventRepository).save(eventCaptor.capture());
        assertEquals("booking-1", eventCaptor.getValue().getBookingId());
        assertEquals("BOOKING_CREATED", eventCaptor.getValue().getEventType());

        ArgumentCaptor<BookingWorkflowSnapshotDocument> snapshotCaptor = ArgumentCaptor.forClass(BookingWorkflowSnapshotDocument.class);
        verify(snapshotRepository).save(snapshotCaptor.capture());
        assertEquals("booking-1", snapshotCaptor.getValue().getBookingId());
        assertEquals("booked", snapshotCaptor.getValue().getCurrentStatus());
        assertEquals("pending", snapshotCaptor.getValue().getPaymentStatus());
        assertEquals(1, snapshotCaptor.getValue().getEventCount());
        assertNotNull(snapshotCaptor.getValue().getUpdatedAt());
    }

    @Test
    void recordSkipsDuplicateEventIds() {
        BookingWorkflowEvent event = new BookingWorkflowEvent();
        event.setEventId("evt-dup");

        when(eventRepository.existsByEventId("evt-dup")).thenReturn(true);

        projectionService.record(event);

        verify(eventRepository, never()).save(any());
        verify(snapshotRepository, never()).save(any());
    }
}
