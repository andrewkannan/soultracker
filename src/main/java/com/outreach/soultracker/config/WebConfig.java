package com.outreach.soultracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path path = Paths.get(uploadDir).toAbsolutePath();
        String location = path.toUri().toString();

        // Spring requires resource locations to end with a slash for directories
        if (!location.endsWith("/")) {
            location += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);

        // Explicitly map og-1.png to the static directory
        registry.addResourceHandler("/og-1.png")
                .addResourceLocations("classpath:/static/");

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                List<MediaType> mediaTypes = new ArrayList<>(jacksonConverter.getSupportedMediaTypes());
                mediaTypes.add(MediaType.valueOf("text/event-stream"));
                jacksonConverter.setSupportedMediaTypes(mediaTypes);
            }
        }
    }
}
