package com.campinglog.campinglogbackserver.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties("refresh")
public class RefreshProperties {

  String secretKey;
  Long expiration;
  String cookie;
}
