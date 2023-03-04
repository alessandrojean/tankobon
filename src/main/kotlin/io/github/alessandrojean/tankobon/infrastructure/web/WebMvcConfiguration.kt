package io.github.alessandrojean.tankobon.infrastructure.web

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.mvc.WebContentInterceptor

@Configuration
class WebMvcConfiguration : WebMvcConfigurer {

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    if (!registry.hasMappingForPattern("/webjars/**")) {
      registry
        .addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/")
    }

    if (!registry.hasMappingForPattern("/swagger-ui.html**")) {
      registry
        .addResourceHandler("/swagger-ui.html**")
        .addResourceLocations("classpath:/META-INF/resources/swagger-ui.html")
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