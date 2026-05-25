package com.criczone.demo.repo;

import com.criczone.demo.domain.TeamDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TeamRepository extends MongoRepository<TeamDocument, String> {
    List<TeamDocument> findByOwner(String owner);
}
