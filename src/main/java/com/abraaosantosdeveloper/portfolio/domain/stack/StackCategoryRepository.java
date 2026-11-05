package com.abraaosantosdeveloper.portfolio.domain.stack;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface StackCategoryRepository extends MongoRepository<StackCategory, String> {

    List<StackCategory> findAllByOrderByOrderAsc();
}