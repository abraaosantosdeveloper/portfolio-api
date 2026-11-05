package com.abraaosantosdeveloper.portfolio.migration;

import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class V002SeedStackCategories implements Migration {

    @Override
    public String id() { return "V002"; }

    @Override
    public String description() { return "Categorias iniciais da stack"; }

    @Override
    public void apply(MongoTemplate mongo) {
        var col = mongo.getCollection("stack_categories");
        if (col.countDocuments() > 0) return; // nunca sobrescreve o que já foi editado
        var now = new Date();
        col.insertMany(List.of(
            category("Linguagens", 1, List.of("Java", "Python", "JavaScript", "SQL"), now),
            category("Back-end", 2, List.of("Spring Boot", "Node.js", "Flask", "APIs REST"), now),
            category("Dados", 3, List.of("PostgreSQL", "MySQL"), now),
            category("Infra & ferramentas", 4, List.of("Git", "GitHub", "Azure", "Linux"), now),
            category("Também mexo com", 5, List.of("Vue.js", "React", "HTML/CSS", "C/C++"), now)
        ));
    }

    private Document category(String title, int order, List<String> items, Date now) {
        return new Document("title", title)
            .append("order", order)
            .append("items", items.stream().map(n -> new Document("name", n)).toList())
            .append("createdAt", now)
            .append("updatedAt", now);
    }
}