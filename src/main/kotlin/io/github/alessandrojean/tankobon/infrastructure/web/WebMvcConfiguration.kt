package io.github.alessandrojean.tankobon.infrastructure.web

import io.github.alessandrojean.tankobon.infrastructure.configuration.TankobonProperties
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.mvc.WebContentInterceptor
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

private val logger = KotlinLogging.logger {}

@Configuration
class WebMvcConfiguration(
  private val properties: TankobonProperties,
) : WebMvcConfigurer {

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    if (!registry.hasMappingForPattern("/webjars/**")) {
      registry
        .addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/")
    }

    if (!registry.hasMappingForPattern("/images/covers/**")) {
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
          "/api/**"
        )
      }
    )
  }

}