package com.campinglog.campinglogbackserver.config;

import com.campinglog.campinglogbackserver.member.repository.MemberRepository;
import com.campinglog.campinglogbackserver.member.service.RefreshTokenService;
import com.campinglog.campinglogbackserver.security.CustomOauth2UserService;
import com.campinglog.campinglogbackserver.security.CustomUserDetailsService;
import com.campinglog.campinglogbackserver.security.JwtAuthenticationFilter;
import com.campinglog.campinglogbackserver.security.JwtBasicAuthenticationFilter;
import com.campinglog.campinglogbackserver.security.JwtProperties;
import com.campinglog.campinglogbackserver.security.JwtTokenProvider;
import com.campinglog.campinglogbackserver.security.OAuth2FailureHandler;
import com.campinglog.campinglogbackserver.security.OAuth2SuccessHandler;
import com.campinglog.campinglogbackserver.security.RefreshProperties;
import com.campinglog.campinglogbackserver.security.RefreshTokenProvider;
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
  private final RefreshProperties refreshProperties;

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
      AuthenticationManager authenticationManager, MemberRepository memberRepository,
      CorsFilter corsFilter, CustomUserDetailsService customUserDetailsService,
      JwtTokenProvider jwtTokenProvider, CustomOauth2UserService customOauth2UserService,
      OAuth2SuccessHandler oAuth2SuccessHandler, OAuth2FailureHandler oAuth2FailureHandler,
      RefreshTokenProvider refreshTokenProvider, RefreshTokenService refreshTokenService
  )
      throws Exception {

    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin(form -> form.disable())
        .httpBasic(httpBasic -> httpBasic.disable());
    http.headers(headers -> headers
        .frameOptions(frame -> frame.disable())); // H2 콘솔용 설정
    // 필터 순서 (매우 중요!)
    http.addFilter(corsFilter);
    http.oauth2Login(oauth2 -> oauth2
        .userInfoEndpoint(userInfo -> userInfo
            .userService(customOauth2UserService))
        .successHandler(oAuth2SuccessHandler)
    );
    http.addFilter(
        new JwtAuthenticationFilter(authenticationManager, jwtTokenProvider, jwtProperties,
            refreshProperties, refreshTokenProvider, refreshTokenService));
    http.addFilter(new JwtBasicAuthenticationFilter(authenticationManager, jwtProperties));
    http.authorizeHttpRequests(auth ->
        auth.requestMatchers("/h2-console/**", "/api/members", "/login", "/favicon.ico",
                "/api/members/refresh").permitAll()
            .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/members/**-availability/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/boards/rank").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/boards/search").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/boards/category").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/boards/*/comments").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/boards/*/likes").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/boards/*").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/boards").hasRole("USER")
            .requestMatchers(HttpMethod.PUT, "/api/boards/*").hasRole("USER")
            .requestMatchers(HttpMethod.DELETE, "/api/boards/*").hasRole("USER")
            .requestMatchers(HttpMethod.DELETE, "/api/members/*").hasRole("USER")
            .requestMatchers(HttpMethod.POST, "/api/boards/*/comments").hasRole("USER")
            .requestMatchers(HttpMethod.POST, "/api/boards/*/likes").hasRole("USER")
            .requestMatchers(HttpMethod.GET, "/api/members/mypage").hasRole("USER")
            .requestMatchers(HttpMethod.POST, "/api/members/mypage/verifyPassword").hasRole("USER")
            .requestMatchers(HttpMethod.PUT, "/api/members/grade").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/members/rank").permitAll()
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
