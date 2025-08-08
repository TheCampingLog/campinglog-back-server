package com.campinglog.campinglogbackserver.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties("jwt")
public class JwtProperties {

  private String issuer;
  private String secretKey;
  private Long expiration;
  private String headerString;
  private String tokenPrefix;

}
