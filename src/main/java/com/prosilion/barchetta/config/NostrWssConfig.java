package com.prosilion.barchetta.config;

import com.prosilion.barchetta.service.nostr.NostrRelayService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.ExecutionException;

@Configuration
@ConditionalOnProperty(
//    prefix = "barchetta",
    name = "server.ssl.enabled",
    havingValue = "true")
public class NostrWssConfig {

  //  @Lazy
  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public NostrRelayService nostrRelayService(
      @NonNull @Value("${superconductor.relay.uri}") String relayUri,
      @NonNull SslBundles sslBundles
  ) throws ExecutionException, InterruptedException {
    return new NostrRelayService(relayUri, sslBundles);
  }
}
