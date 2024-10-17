package com.prosilion.barchetta.config;

import com.prosilion.presto.NostrSecurityConfig;
import com.prosilion.presto.nostr.controller.NostrAuthController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableWebSocketSecurity
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
    http.authorizeHttpRequests(authorize -> authorize
        .requestMatchers(
            mvc.pattern("/contract/**")).hasRole("USER")
    );
    return http.build();
  }

  @Bean
  AuthorizationManager<Message<?>> authorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
    messages
        //        .simpDestMatchers("/user/queue/errors").permitAll()
        //        .simpDestMatchers("/admin/**").hasRole("ADMIN")
        .simpDestMatchers("/**").permitAll()
        .anyMessage().authenticated();
    return messages.build();
  }
}
