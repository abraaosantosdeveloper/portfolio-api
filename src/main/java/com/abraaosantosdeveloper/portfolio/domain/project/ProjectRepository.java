package com.abraaosantosdeveloper.portfolio.domain.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectRepository extends MongoRepository<Project, String> {
    List<Project> findAllByPublishedTrueOrderByYearDesc();

    Optional<Project> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
