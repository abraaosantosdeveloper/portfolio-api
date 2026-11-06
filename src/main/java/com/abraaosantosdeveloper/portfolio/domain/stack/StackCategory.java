package com.abraaosantosdeveloper.portfolio.domain.stack;

import java.time.Instant;
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

@Document("stack_categories")
@Getter
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class StackCategory {
    @Id 
    private String id;

    private String title;
    private String description;
    private int order;

    @Builder.Default
    private List<StackItem> items = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

}
