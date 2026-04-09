package com.shortly.kgsservice;

import com.shortly.kgsservice.enumaration.StatusType;
import com.shortly.kgsservice.model.ShortlyKey;
import com.shortly.kgsservice.repository.ShortlyKeyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
class KgsServiceApplicationTests {

	@Autowired
	ShortlyKeyRepository shortlyKeyRepository;

	@Test
	void contextLoads() {
	}


	@Test
	void shouldCreateCollectionWhenInsertData() {
		ShortlyKey shortlyKey = ShortlyKey.builder()
				.key("xyzcvic")
				.status(StatusType.AVAILABLE)
				.createdAt(LocalDateTime.now())
				.build();
		shortlyKeyRepository.save(shortlyKey);
		long count = shortlyKeyRepository.count();
		System.out.println("Total data: " + count);
	}
}
