package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  private UserDetailsService userDetailsService;

  public SecurityConfig(UserDetailsService userSvc) {
    this.userDetailsService = userSvc;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth
      .userDetailsService(userDetailsService)
      .passwordEncoder(encoder());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .cors().and().csrf().disable();
    http
      .requiresChannel()
      .anyRequest()
      .requiresSecure();
    http
      .httpBasic()
        .and()
      .authorizeRequests()
        .antMatchers(HttpMethod.GET, "/all", "/all/**").hasAnyRole("MANAGER", "ANALYST", "USER")
        .antMatchers(HttpMethod.GET, "/customers", "/customers/**").hasAnyRole("MANAGER", "ANALYST", "USER")
        .antMatchers(HttpMethod.POST, "/customers", "/customers/**").hasRole("MANAGER")
        .antMatchers(HttpMethod.PUT, "/customers", "/customers/**").hasAnyRole("MANAGER", "ANALYST", "USER")
        .antMatchers(HttpMethod.GET, "/content", "/content/**").hasAnyRole("MANAGER", "ANALYST", "USER")
        .antMatchers(HttpMethod.DELETE, "/content", "/content/**").hasAnyRole("MANAGER","ANALYST")
        .antMatchers(HttpMethod.POST, "/content", "/content/**").hasAnyRole("MANAGER", "ANALYST")
        .antMatchers(HttpMethod.PUT, "/content", "/content/**").hasAnyRole("MANAGER", "ANALYST")
        .and()
      .formLogin().disable();
  }

  @Bean
  public BCryptPasswordEncoder encoder() {
    // auto-generate a random salt internally
    return new BCryptPasswordEncoder();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("*"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}