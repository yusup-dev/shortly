package com.shortly.kgsservice.repository;

import com.shortly.kgsservice.model.ShortlyKey;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortlyKeyRepository extends MongoRepository<ShortlyKey, ObjectId> {
    Optional<ShortlyKey> findByKey(String key);
}
