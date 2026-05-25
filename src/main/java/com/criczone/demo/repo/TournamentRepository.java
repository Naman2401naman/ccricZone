package com.criczone.demo.repo;

import com.criczone.demo.domain.TournamentDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TournamentRepository extends MongoRepository<TournamentDocument, String> {
    List<TournamentDocument> findByStatus(String status);
}
