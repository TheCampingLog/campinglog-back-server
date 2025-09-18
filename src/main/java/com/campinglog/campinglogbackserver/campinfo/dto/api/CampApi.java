package com.campinglog.campinglogbackserver.campinfo.dto.api;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.camp")
@Data
public class CampApi {
    private String baseUrl;
    private String apiKey;
}
