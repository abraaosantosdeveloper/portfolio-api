package com.abraaosantosdeveloper.portfolio.migration;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("migrations")
public record MigrationRecord(@Id String id, String description, Instant appliedAt) {}
