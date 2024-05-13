package com.prosilion.scdecisionmatrix.config;

import edu.mayo.lpea.cad.cadence3.JpaSecurityConfig;
import edu.mayo.lpea.cad.cadence3.jpa.controller.JpaAuthController;
import edu.mayo.lpea.cad.cadence3.web.controller.AuthController;
import edu.mayo.lpea.cad.cadence3.web.controller.UsersController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = "edu.mayo.lpea.cad.cadence3.*")
// TODO: below should not be necessary, revisit
@Import({JpaSecurityConfig.class, UsersController.class, JpaAuthController.class, AuthController.class})
public class WebSecurityConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);

  @Bean
  @Primary
  public AuthenticationSuccessHandler customAuthenticationSuccessHandler(){
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
}
