package io.github.alessandrojean.tankobon.infrastructure.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.convert.DurationUnit
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import java.time.Duration
import java.time.temporal.ChronoUnit
import javax.validation.constraints.NotBlank

@Component
@ConfigurationProperties(prefix = "tankobon")
@Validated
class TankobonProperties {
  var database = Database()

  var cors = Cors()

  var configDir: String? = null

  @DurationUnit(ChronoUnit.SECONDS)
  var sessionTimeout: Duration = Duration.ofMinutes(30)

  class Cors {
    var allowedOrigins: List<String> = emptyList()
  }

  class Database {
    @get:NotBlank
    var file: String = ""
  }
}
