package com.criczone.demo.repo;

import com.criczone.demo.domain.UserDocument;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByEmail(String email);
    Optional<UserDocument> findByPhone(String phone);
}
