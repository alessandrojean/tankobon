package io.github.alessandrojean.tankobon.infrastructure.configuration

import org.springframework.context.annotation.Bean
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Component
class WebClientConfiguration {

  @Bean
  fun webClient() = WebClient.builder()
    .clientConnector(ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
    .build()
}
