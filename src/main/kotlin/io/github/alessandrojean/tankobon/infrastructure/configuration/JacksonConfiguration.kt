package io.github.alessandrojean.tankobon.infrastructure.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Component
import org.zalando.jackson.datatype.money.MoneyModule
import java.time.format.DateTimeFormatter

@Component
class JacksonConfiguration {

  private val dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"

  @Bean
  fun objectMapper(): Jackson2ObjectMapperBuilder =
    Jackson2ObjectMapperBuilder()
      .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .featuresToDisable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
      .simpleDateFormat(dateTimeFormat)
      .serializers(LocalDateTimeSerializer(DateTimeFormatter.ofPattern(dateTimeFormat)))
      .modulesToInstall(
        MoneyModule().withFastMoney(),
        JavaTimeModule(),
        ParameterNamesModule(),
        Jdk8Module(),
        KotlinModule.Builder().build(),
      )
}
