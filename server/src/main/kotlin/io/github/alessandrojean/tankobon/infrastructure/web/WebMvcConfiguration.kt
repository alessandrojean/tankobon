package io.github.alessandrojean.tankobon.infrastructure.web

import io.github.alessandrojean.tankobon.infrastructure.configuration.TankobonProperties
import mu.KotlinLogging
import org.springframework.boot.convert.ApplicationConversionService
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.http.CacheControl
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.mvc.WebContentInterceptor
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString

private val logger = KotlinLogging.logger {}

@Configuration
class WebMvcConfiguration(
  private val properties: TankobonProperties,
) : WebMvcConfigurer {

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    registry
      .addResourceHandler(
        "/index.html",
        "/favicon.svg",
      )
      .addResourceLocations(
        "classpath:public/index.html",
        "classpath:public/favicon.svg",
      )
      .setCacheControl(CacheControl.noStore())

    registry
      .addResourceHandler(
        "/fonts/**",
        "/assets/**",
        "/flags/**",
      )
      .addResourceLocations(
        "classpath:public/fonts/",
        "classpath:public/assets/",
        "classpath:public/flags/",
      )
      .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())

    if (!registry.hasMappingForPattern("/webjars/**")) {
      registry
        .addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/")
    }

    if (!registry.hasMappingForPattern("/images/**")) {
      val imagesDirectoryPath = Paths.get(properties.imagesDir).absolutePathString()

      registry
        .addResourceHandler("/images/**")
        .addResourceLocations("file:$imagesDirectoryPath/")
    }
  }

  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(
      WebContentInterceptor().apply {
        addCacheMapping(
          cachePrivate,
          "/api/**",
        )
      },
    )
  }

  override fun addFormatters(registry: FormatterRegistry) {
    // Add provided custom converters such as enum to string.
    ApplicationConversionService.configure(registry)
  }
}

@Component
@ControllerAdvice
class Customizer {

  @ExceptionHandler(NoHandlerFoundException::class)
  fun notFound(): String = "forward:/"
}
