package io.github.alessandrojean.tankobon.infrastructure.swagger

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.tags.Tag
import mu.KotlinLogging
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.customizers.OperationCustomizer
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

private val logger = KotlinLogging.logger {}

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
            "Basic Auth",
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
      .addTagsItem(
        Tag()
          .name("3. Reference Expansion")
          .description(
            """
              Some endpoints supports the reference expansion feature, which allows relationships
              of a resource to be expanded with their attributes, reducing the amount of requests
              that need to be sent to the API to retrieve a complete set of data.
              
              Endpoints that supports this feature are indicated by the presence of an optional
              `includes` query parameter, which can be set to a list of includes with the type
              names from the relationships, in lowercase, separated by a comma (`,`).
            """.trimIndent()
          )
      )

  @Bean
  fun customizeOpenApi(): OpenApiCustomizer = OpenApiCustomizer { openApi ->
    openApi.tags = openApi.tags
      .sortedBy { it.name }
      .map { it.apply { name = name.replace(NUMERIC_HEADER_REGEX, "") } }
  }

  @Bean
  fun customizeOperations(): OperationCustomizer = OperationCustomizer { operation, handlerMethod ->
    logger.info { operation.operationId }

    when {
      operation.operationId.contains("^(create|update|import|add)".toRegex()) -> {
        val contentType = Parameter()
          .`in`(ParameterIn.HEADER.toString())
          .schema(StringSchema().apply { setDefault(MediaType.APPLICATION_JSON_VALUE) })
          .name(HttpHeaders.CONTENT_TYPE)
          .required(true)

        operation.apply { addParametersItem(contentType) }
      }
      operation.operationId.startsWith("upload") -> {
        val contentType = Parameter()
          .`in`(ParameterIn.HEADER.toString())
          .schema(StringSchema().apply { setDefault(MediaType.MULTIPART_FORM_DATA_VALUE) })
          .name(HttpHeaders.CONTENT_TYPE)
          .required(true)

        operation.apply { addParametersItem(contentType) }
      }
      else -> operation
    }
  }

  @Bean
  fun modelResolver(objectMapper: ObjectMapper) = ModelResolver(objectMapper)

  init {
    // https://github.com/springdoc/springdoc-openapi/issues/66
    SpringDocUtils.getConfig()
      .replaceWithClass(javax.money.MonetaryAmount::class.java, MonetaryAmountDto::class.java)
  }

  data class MonetaryAmountDto(
    @get:Schema(example = "10.99")
    val amount: Float,
    @get:Schema(
      format = "iso-4217",
      pattern = "^[A-Z]{3}$",
      example = "USD",
      description = "[ISO 4217](https://en.wikipedia.org/wiki/ISO_4217#List_of_ISO_4217_currency_codes) currency code"
    )
    val currency: String
  )

  companion object {
    private val NUMERIC_HEADER_REGEX = "^\\d+\\. ".toRegex()
  }

}