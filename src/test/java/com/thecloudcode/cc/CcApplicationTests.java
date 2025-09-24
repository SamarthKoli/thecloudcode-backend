package com.thecloudcode.cc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.mail.username=test@example.com",
    "spring.mail.password=test",
    "openai.api.key=test-key",
    "admin.allowed.ips=127.0.0.1",
    "cors.allowed-origins=http://localhost:3000"
})
class CcApplicationTests {

    @Test
    void contextLoads() {
        // This test just verifies that the Spring context loads successfully
    }
}
