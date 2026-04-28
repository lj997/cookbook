package com.cookbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {OpenAiAutoConfiguration.class})
public class CookbookApplication {
    public static void main(String[] args) {
        SpringApplication.run(CookbookApplication.class, args);
    }
}
