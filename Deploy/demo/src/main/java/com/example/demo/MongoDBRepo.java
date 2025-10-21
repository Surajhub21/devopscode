package com.example.demo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoDBRepo extends MongoRepository<Notes , String> {
}
