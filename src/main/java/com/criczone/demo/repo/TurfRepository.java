package com.criczone.demo.repo;

import com.criczone.demo.domain.TurfDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TurfRepository extends MongoRepository<TurfDocument, String> {
    List<TurfDocument> findByOwnerId(String ownerId);
    List<TurfDocument> findByIsActiveTrue();
}
