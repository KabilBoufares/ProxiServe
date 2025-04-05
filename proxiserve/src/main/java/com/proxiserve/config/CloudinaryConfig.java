package com.proxiserve.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dgj4bnb3n",
            "api_key", "311667947958616",
            "api_secret", "8P2-iIPP-2BBOPQxgsLwe7btZng",
            "secure", true
        ));
    }
}
