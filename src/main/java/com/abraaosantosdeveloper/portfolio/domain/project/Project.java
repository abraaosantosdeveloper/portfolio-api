package com.abraaosantosdeveloper.portfolio.domain.project;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document("projects")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class Project {
    @Id 
    private String id;

    private String slug;

    private String title;

    private String description;

    private String problem;
    
    private String solution;

    private Integer year;

    private ProjectStatus status;

    private boolean published;

    private String icon;

    private String color;

    @Builder.Default
    private List<ProjectImage> images = new ArrayList<>();

    private String repoUrl;
    private String liveUrl;

    @Field(targetType = FieldType.OBJECT_ID)
    private String authorId;

    @CreatedDate 
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
