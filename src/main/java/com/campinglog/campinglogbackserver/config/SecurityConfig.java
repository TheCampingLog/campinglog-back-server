package com.campinglog.campinglogbackserver.config;

import com.campinglog.campinglogbackserver.member.repository.MemberRepository;
import com.campinglog.campinglogbackserver.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                                                 AuthenticationManager authenticationManager, MemberRepository memberRepository,
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
            auth.requestMatchers("/h2-console/**", "/api/members", "/login").permitAll()
                    .requestMatchers(HttpMethod.GET,  "/api/members/**-availability/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/boards/rank").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/boards/search").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/boards/category").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/boards/*/comments").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/boards/*/likes").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/boards/*").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/boards").hasRole("USER")
                    .requestMatchers(HttpMethod.PUT, "/api/boards/*").hasRole("USER")
                    .requestMatchers(HttpMethod.DELETE, "/api/boards/*").hasRole("USER")
                    .requestMatchers(HttpMethod.POST, "/api/boards/*/comments").hasRole("USER")
                    .requestMatchers(HttpMethod.POST, "/api/boards/*/likes").hasRole("USER")
                    .requestMatchers(HttpMethod.GET,  "/api/members/mypage").hasRole("USER")
                    .requestMatchers(HttpMethod.POST,  "/api/members/mypage/verifyPassword").hasRole("USER")
                    .requestMatchers(HttpMethod.PUT,  "/api/members/grade").permitAll()
                    .requestMatchers("/api/members/test").hasAnyRole("USER")
                    .requestMatchers("/api/camps/members/**").hasRole("USER")
                    .requestMatchers("/api/camps/**").permitAll()
                    .requestMatchers("/error").permitAll()
                    .anyRequest().hasAnyRole("USER"));

    return http.build();
  }

  @Bean
  public BCryptPasswordEncoder BCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }


}
