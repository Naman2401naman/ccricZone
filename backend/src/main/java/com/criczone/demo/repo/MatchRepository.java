package com.criczone.demo.repo;

import com.criczone.demo.domain.MatchDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MatchRepository extends MongoRepository<MatchDocument, String> {
    List<MatchDocument> findByCreatedByOrderByMatchDateDesc(String createdBy);
}
