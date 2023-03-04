package io.github.alessandrojean.tankobon.infrastructure.web

import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import java.util.concurrent.TimeUnit

fun ResponseEntity.BodyBuilder.setCachePrivate() =
  this.cacheControl(cachePrivate)

val cachePrivate = CacheControl
  .maxAge(0, TimeUnit.SECONDS)
  .noTransform()
  .cachePrivate()
  .mustRevalidate()
