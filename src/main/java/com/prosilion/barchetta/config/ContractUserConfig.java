package com.prosilion.barchetta.config;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.ContractAppUser;
import com.prosilion.barchetta.repository.ContractRepository;
import com.prosilion.barchetta.repository.ContractUserRepository;
import com.prosilion.presto.security.service.CustomizableAppUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Slf4j
@Configuration
@EnableJpaRepositories(basePackageClasses = {
    ContractUserRepository.class
    , ContractRepository.class
})
@EntityScan(basePackageClasses = {
    ContractAppUser.class
    , Contract.class
})
@ComponentScan(basePackages = "com.prosilion.presto.*")
public class ContractUserConfig {

  @Bean
  CustomizableAppUserService customizableAppUserService() {
    log.info("CONTRACT USER CONFIG - Creating ContractAppUser");
    return new CustomizableAppUserService(new ContractAppUser());
  }

//  @Bean
//  CustomizablePayerService payerService() {
//    log.info("Creating Payer");
//    return new CustomizablePayerService(new Payer());
//  }
//
//  @Bean
//  CustomizablePayeeService customizablePayeeService() {
//    log.info("Creating Payee");
//    return new CustomizablePayeeService(new Payee());
//  }
}
