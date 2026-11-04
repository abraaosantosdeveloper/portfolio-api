package com.abraaosantosdeveloper.portfolio.domain.timeline;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document("timeline_phases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimelinePhase {
    @Id 
    private String id;

    private PhaseKind kind;

    private String title;

    private String description;

    private String institution;

    @Builder.Default
    private List<String> technologies = new ArrayList<>();

    private LocalDate startDate;

    private LocalDate endDate; // null = em andamento

    @CreatedDate 
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
    
}
