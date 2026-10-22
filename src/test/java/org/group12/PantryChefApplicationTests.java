package org.group12;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        // Giá trị giả để Spring resolve được các placeholder không có default trong application.properties
        "CLOUDINARY_CLOUD_NAME=test",
        "CLOUDINARY_API_KEY=test",
        "CLOUDINARY_API_SECRET=test",
        "GMAIL_USERNAME=test@example.com",
        "GMAIL_APP_PASSWORD=test",
        "REDIS_PASSWORD="
})
@ActiveProfiles("test")
class PantryChefApplicationTests {

	@Test
	void contextLoads() {
	}

}
