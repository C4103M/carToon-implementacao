package com.cartoon.api;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = ApiApplicationTests.TestConfig.class)
class ApiApplicationTests {

	@Configuration
	static class TestConfig {}

	@Test
	void contextLoads() {
	}

}
