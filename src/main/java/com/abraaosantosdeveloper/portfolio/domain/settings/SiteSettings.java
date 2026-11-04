package com.abraaosantosdeveloper.portfolio.domain.settings;

import java.time.Instant;

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

@Document("site_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSettings {
    public static final String ID = "site";
    
    @Id
    private String id;

    @Field(targetType = FieldType.OBJECT_ID)
    private String currentPhaseId;

    private String nextPhaseText;

    @LastModifiedDate
    private Instant updatedAt;

}
