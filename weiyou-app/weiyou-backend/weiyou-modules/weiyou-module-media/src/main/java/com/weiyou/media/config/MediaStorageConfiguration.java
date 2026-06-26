package com.weiyou.media.config;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class MediaStorageConfiguration implements WebMvcConfigurer {

    @Value("${weiyou.media.local-dir:./data/uploads}")
    private String localDir;

    @PostConstruct
    public void ensureDirectory() throws Exception {
        Files.createDirectories(resolveLocalDir());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resolveLocalDir().toUri().toString());
    }

    private Path resolveLocalDir() {
        return Paths.get(localDir).toAbsolutePath().normalize();
    }
}
