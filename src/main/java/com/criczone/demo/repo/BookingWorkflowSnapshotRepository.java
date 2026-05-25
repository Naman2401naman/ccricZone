package com.criczone.demo.repo;

import com.criczone.demo.domain.BookingWorkflowSnapshotDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookingWorkflowSnapshotRepository extends MongoRepository<BookingWorkflowSnapshotDocument, String> {
    List<BookingWorkflowSnapshotDocument> findByOwnerIdOrderByUpdatedAtDesc(String ownerId);
    List<BookingWorkflowSnapshotDocument> findByUserIdOrderByUpdatedAtDesc(String userId);
}
