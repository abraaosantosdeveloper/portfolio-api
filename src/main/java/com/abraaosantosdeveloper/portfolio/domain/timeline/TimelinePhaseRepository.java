package com.abraaosantosdeveloper.portfolio.domain.timeline;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TimelinePhaseRepository extends MongoRepository<TimelinePhase, String> {

    List<TimelinePhase> findAllByOrderByStartDateDesc();
}