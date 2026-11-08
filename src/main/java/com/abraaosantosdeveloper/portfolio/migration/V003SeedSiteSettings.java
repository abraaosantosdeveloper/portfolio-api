package com.abraaosantosdeveloper.portfolio.migration;

import java.util.Date;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.model.Filters;

@Component
public class V003SeedSiteSettings implements Migration {

    @Override
    public String id() { return "V003"; }

    @Override
    public String description() { return "Documento único de configurações do site"; }

    @Override
    public void apply(MongoTemplate mongo) {
        var col = mongo.getCollection("site_settings");
        if (col.countDocuments(Filters.eq("_id", "site")) > 0) return;
        col.insertOne(new Document("_id", "site")
            .append("currentPhaseId", null)
            .append("nextPhaseText", "Quem sabe na sua empresa?")
            .append("updatedAt", new Date()));
    }
}