package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.sql.init.mode=never",

		// 🟢 AJOUTS CRUCIAUX POUR HIBERNATE 6 :
		"spring.jpa.properties.hibernate.default_schema=",
		"spring.jpa.properties.hibernate.connection.charSet=UTF-8"
})
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}