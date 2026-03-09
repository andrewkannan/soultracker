package com.outreach.soultracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = uploadDir;
        if (!path.endsWith("/")) {
            path += "/";
        }

        // Ensure absolute paths are converted to valid file coordinates
        String location = path.startsWith("/") ? "file:" + path : "file:./" + path;

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
