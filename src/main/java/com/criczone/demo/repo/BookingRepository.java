package com.criczone.demo.repo;

import com.criczone.demo.domain.BookingDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookingRepository extends MongoRepository<BookingDocument, String> {
    List<BookingDocument> findByUserOrderByCreatedAtDesc(String userId);
    List<BookingDocument> findByTurfAndDateAndStatus(String turfId, String date, String status);
}
