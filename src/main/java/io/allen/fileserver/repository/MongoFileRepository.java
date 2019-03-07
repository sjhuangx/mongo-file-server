package io.allen.fileserver.repository;

import io.allen.fileserver.domain.MongoFile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoFileRepository extends MongoRepository<MongoFile, String> {
}
