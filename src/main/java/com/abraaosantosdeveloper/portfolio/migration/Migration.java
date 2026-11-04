package com.abraaosantosdeveloper.portfolio.migration;

import org.springframework.data.mongodb.core.MongoTemplate;

public interface Migration {
    String id();
    String description();
    
    void apply(MongoTemplate mongo);
}
