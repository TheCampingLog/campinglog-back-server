package com.campinglog.campinglogbackserver.config;

import com.campinglog.campinglogbackserver.account.repository.MemberRespository;
import com.campinglog.campinglogbackserver.security.CustomUserDetailsService;
import com.campinglog.campinglogbackserver.security.JwtAuthenticationFilter;
import com.campinglog.campinglogbackserver.security.JwtBasicAuthenticationFilter;
import com.campinglog.campinglogbackserver.security.JwtProperties;
import com.campinglog.campinglogbackserver.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtProperties jwtProperties;

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
      AuthenticationManager authenticationManager, MemberRespository memberRespository,
      CorsFilter corsFilter, CustomUserDetailsService customUserDetailsService,
      JwtTokenProvider jwtTokenProvider) throws Exception {

    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin(form -> form.disable())
        .httpBasic(httpBasic -> httpBasic.disable());
    http.headers(headers -> headers
        .frameOptions(frame -> frame.disable())); // H2 콘솔용 설정
    http.addFilter(corsFilter);
    http.addFilter(
        new JwtAuthenticationFilter(authenticationManager, jwtTokenProvider, jwtProperties));
    http.addFilter(new JwtBasicAuthenticationFilter(authenticationManager, jwtProperties));
    http.authorizeHttpRequests(auth ->
        auth.requestMatchers("/h2-console/**", "/api/account", "/login").permitAll() // H2 콘솔 허용
            .requestMatchers("/api/account/test").hasAnyRole("ADMIN", "USER")
            .anyRequest().hasAnyRole("ADMIN", "USER"));

    return http.build();
  }

  @Bean
  public BCryptPasswordEncoder BCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }


}
