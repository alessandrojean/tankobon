package io.github.alessandrojean.tankobon.infrastructure.swagger

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfiguration {

  @Bean
  fun openApi(): OpenAPI =
    OpenAPI()
      .info(
        Info()
          .title("Tankobon API")
          .version("v1.0")
          .description(
            """
              Tankobon offers a REST API.
               
              The API is secured using HTTP Basic Authentication.
            """.trimIndent(),
          )
          .license(
            License()
              .name("MIT")
              .url("https://github.com/alessandrojean/tankobon/blob/master/LICENSE")
          ),
      )
      .externalDocs(
        ExternalDocumentation()
          .description("Tankobon documentation")
          .url("https://alessandrojean.github.io/tankobon"),
      )
      .components(
        Components()
          .addSecuritySchemes(
            "basicAuth",
            SecurityScheme()
              .type(SecurityScheme.Type.HTTP)
              .scheme("basic"),
          ),
      )
}