package com.criczone.demo.workflow.booking;

import com.criczone.demo.domain.BookingWorkflowEventDocument;
import com.criczone.demo.domain.BookingWorkflowSnapshotDocument;
import com.criczone.demo.repo.BookingWorkflowEventRepository;
import com.criczone.demo.repo.BookingWorkflowSnapshotRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class BookingWorkflowProjectionService {

    private final BookingWorkflowEventRepository eventRepository;
    private final BookingWorkflowSnapshotRepository snapshotRepository;

    public BookingWorkflowProjectionService(BookingWorkflowEventRepository eventRepository,
                                            BookingWorkflowSnapshotRepository snapshotRepository) {
        this.eventRepository = eventRepository;
        this.snapshotRepository = snapshotRepository;
    }

    public void record(BookingWorkflowEvent event) {
        if (eventRepository.existsByEventId(event.getEventId())) {
            return;
        }

        BookingWorkflowEventDocument eventDocument = new BookingWorkflowEventDocument();
        eventDocument.setEventId(event.getEventId());
        eventDocument.setBookingId(event.getBookingId());
        eventDocument.setTurfId(event.getTurfId());
        eventDocument.setUserId(event.getUserId());
        eventDocument.setOwnerId(event.getOwnerId());
        eventDocument.setEventType(event.getEventType().name());
        eventDocument.setBookingStatus(event.getBookingStatus());
        eventDocument.setPaymentStatus(event.getPaymentStatus());
        eventDocument.setAmount(event.getAmount());
        eventDocument.setTriggeredByUserId(event.getTriggeredByUserId());
        eventDocument.setSource(event.getSource());
        eventDocument.setRequestId(event.getRequestId());
        eventDocument.setMetadata(event.getMetadata());
        eventDocument.setOccurredAt(event.getOccurredAt());
        eventDocument.setConsumedAt(Instant.now());
        eventRepository.save(eventDocument);

        BookingWorkflowSnapshotDocument snapshot = snapshotRepository.findById(event.getBookingId())
            .orElseGet(BookingWorkflowSnapshotDocument::new);
        if (snapshot.getBookingId() == null) {
            snapshot.setBookingId(event.getBookingId());
            snapshot.setCreatedAt(event.getOccurredAt());
        }
        snapshot.setTurfId(event.getTurfId());
        snapshot.setUserId(event.getUserId());
        snapshot.setOwnerId(event.getOwnerId());
        snapshot.setCurrentStatus(event.getBookingStatus());
        snapshot.setPaymentStatus(event.getPaymentStatus());
        snapshot.setLastEventType(event.getEventType().name());
        snapshot.setTotalAmount(event.getAmount());
        snapshot.setLastRequestId(event.getRequestId());
        snapshot.setLatestMetadata(event.getMetadata());
        snapshot.setLastEventAt(event.getOccurredAt());
        snapshot.setUpdatedAt(Instant.now());
        snapshot.setEventCount((snapshot.getEventCount() == null ? 0 : snapshot.getEventCount()) + 1);
        snapshotRepository.save(snapshot);
    }
}
