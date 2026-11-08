package com.abraaosantosdeveloper.portfolio.migration;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

@Component
public class V001CreateIndexes implements Migration {

    @Override
    public String id() { return "V001"; }

    @Override
    public String description() { return "Cria os índices das coleções"; }

    @Override
    public void apply(MongoTemplate mongo) {
        mongo.getCollection("users")
             .createIndex(Indexes.ascending("username"), new IndexOptions().unique(true));
        mongo.getCollection("projects")
             .createIndex(Indexes.ascending("slug"), new IndexOptions().unique(true));
        mongo.getCollection("projects")
             .createIndex(Indexes.compoundIndex(Indexes.ascending("published"), Indexes.descending("year")));
        mongo.getCollection("timeline_phases")
             .createIndex(Indexes.descending("startDate"));
        mongo.getCollection("stack_categories")
             .createIndex(Indexes.ascending("order"));
    }
}