package com.prosilion.scdecisionmatrix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class ScdecisionmatrixApplication extends SpringBootServletInitializer {
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(ScdecisionmatrixApplication.class);
  }
  public static void main(String[] args) {
    SpringApplication.run(ScdecisionmatrixApplication.class, args);
  }
}
