package com.abraaosantosdeveloper.portfolio;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
    "app.admin.username=test-admin",
    "app.admin.password-hash=$2y$12$testtesttesttesttesttesttesttesttesttesttesttesttesttes"
})
class PortfolioApplicationTests {

    @Test
    void contextLoads() {
    }
}