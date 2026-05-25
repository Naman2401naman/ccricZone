package com.criczone.demo.repo;

import com.criczone.demo.domain.BookingWorkflowEventDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookingWorkflowEventRepository extends MongoRepository<BookingWorkflowEventDocument, String> {
    boolean existsByEventId(String eventId);
    List<BookingWorkflowEventDocument> findByBookingIdOrderByOccurredAtAsc(String bookingId);
    List<BookingWorkflowEventDocument> findByOwnerIdOrderByOccurredAtDesc(String ownerId);
    List<BookingWorkflowEventDocument> findByUserIdOrderByOccurredAtDesc(String userId);
}
