package com.secure.connect.secure_connect.user.repository;

import com.secure.connect.secure_connect.user.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
}
