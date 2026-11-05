package com.abraaosantosdeveloper.portfolio.migration;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(1) // antes do AdminSeeder
@RequiredArgsConstructor
public class MigrationRunner implements ApplicationRunner {
    private final MongoTemplate mongo;
    private final List<Migration> migrations;

    @Override
    public void run(ApplicationArguments args){
        migrations.stream()
        .sorted(Comparator.comparing(Migration::id))
            .filter(m -> !mongo.exists(Query.query(Criteria.where("_id").is(m.id())), MigrationRecord.class))
        .forEach(m -> {
            log.info("Aplicando migration {} - {}", m.id(), m.description());
            m.apply(mongo);
            mongo.insert(new MigrationRecord(m.id(), m.description(), Instant.now()));
        });
    }
}
