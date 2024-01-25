package com.github.app;

import com.github.security.AuthenticationController;
import com.github.security.Encoding;
import com.github.security.UserDetailServiceImp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {
	AuthenticationController.class,
	UserDetailServiceImp.class,
	Encoding.class
})
class TransactionServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
