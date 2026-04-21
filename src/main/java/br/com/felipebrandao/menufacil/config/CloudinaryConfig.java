package br.com.felipebrandao.menufacil.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
@EnableConfigurationProperties(CloudinaryProperties.class)
public class CloudinaryConfig {

    @Bean
    @ConditionalOnProperty(prefix = "cloudinary", name = {"cloud-name", "api-key", "api-secret"})
    public Cloudinary cloudinary(CloudinaryProperties properties) {
        Assert.hasText(properties.cloudName(), "cloudinary.cloud-name must not be blank");
        Assert.hasText(properties.apiKey(), "cloudinary.api-key must not be blank");
        Assert.hasText(properties.apiSecret(), "cloudinary.api-secret must not be blank");

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", properties.cloudName(),
                "api_key", properties.apiKey(),
                "api_secret", properties.apiSecret(),
                "secure", true
        ));
    }
}

