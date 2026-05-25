package com.criczone.demo.repo;

import com.criczone.demo.domain.PostDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<PostDocument, String> {
    List<PostDocument> findByUserIdOrderByCreatedAtDesc(String userId);
}
