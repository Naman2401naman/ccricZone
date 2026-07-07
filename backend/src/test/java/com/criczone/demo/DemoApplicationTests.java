package com.criczone.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.jwt.secret=01234567890123456789012345678901")
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
