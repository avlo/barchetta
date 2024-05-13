package com.prosilion.scdecisionmatrix.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

// TODO: refactor below + application.properties key=value into cad3 framework
public class ContractDefaultLoginHandler implements AuthenticationSuccessHandler {
  @Value("${scd.login.url.successful}")
  private String successUrl;
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
    new DefaultRedirectStrategy().sendRedirect(request, response, successUrl);
  }
}
