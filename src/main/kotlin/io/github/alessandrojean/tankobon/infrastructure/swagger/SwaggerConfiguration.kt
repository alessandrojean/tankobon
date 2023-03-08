package io.github.alessandrojean.tankobon.infrastructure.swagger

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.tags.Tag
import org.springdoc.core.customizers.OpenApiCustomizer
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
      .addTagsItem(
        Tag()
          .name("1. Authentication")
          .description(
            """
              The Tankobon API is secured using HTTP Basic Authentication. This means in order
              to authenticate yourself, you need to send your username (the e-mail) and
              password through a simple `Authorization` HTTP header with the basic
              base 64 encoding preceded by the keyword `Basic`.
              
              Requests done with the HTTP Basic Authentication will set a `SESSION` cookie
              with the authenticated session, which can be reused if needed. Alternatively,
              you can also send a `X-Auth-Token` header. If sent empty, the response will
              include a header with the same name with the authenticated session token that
              can be reused in future requests without requiring the HTTP Basic Authentication.
            """.trimIndent()
          )
      )
      .addTagsItem(
        Tag()
          .name("2. Date and time")
          .description(
            """
              The API expects all date-time values sent in creation/update operations to be in
              the [UTC](https://en.wikipedia.org/wiki/Coordinated_Universal_Time) timezone.
              In the same way, all returned date-time values will be in the UTC timezone as well,
              which can be converted to the user local timezone when you need to show it.
              
              Make sure you're converting the date-time values to UTC when sending the requests,
              otherwise you will create some time inconsistencies in the database that can lead
              to wrong date-time values when presenting.
            """.trimIndent()
          )
      )

  @Bean
  fun customizeOpenApi(): OpenApiCustomizer = OpenApiCustomizer { openApi ->
    openApi.tags = openApi.tags
      .sortedBy { it.name }
      .map { it.apply { name = name.replace(NUMERIC_HEADER_REGEX, "") } }
  }

  companion object {
    private val NUMERIC_HEADER_REGEX = "^\\d+\\. ".toRegex()
  }

}