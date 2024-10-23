package com.prosilion.barchetta.config;

import com.prosilion.presto.NostrSecurityConfig;
import com.prosilion.presto.nostr.controller.NostrAuthController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableWebSocketSecurity
@EnableMethodSecurity
@ComponentScan(basePackages = {"com.prosilion.presto.nostr.*"})
// TODO: below should not be necessary, revisit
@Import({NostrSecurityConfig.class, NostrAuthController.class})
public class WebSecurityConfig {

  @Bean
  @Primary
  public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
    return new ContractDefaultLoginHandler();
  }

  @Bean
  public SecurityFilterChain scdFilterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
    http
//        TODO: below should redirect http requests to https, but does not.  revisit
        .requiresChannel(channel -> channel
            .anyRequest().requiresSecure()
        ).authorizeHttpRequests(authorize -> authorize
            .requestMatchers(mvc.pattern("/")).permitAll()
            .requestMatchers(mvc.pattern("/index.html")).permitAll()
            .requestMatchers(mvc.pattern("/contract/**")).hasRole("USER")
        );
    return http.build();
  }
}
