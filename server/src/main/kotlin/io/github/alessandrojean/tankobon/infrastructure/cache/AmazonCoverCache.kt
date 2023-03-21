package io.github.alessandrojean.tankobon.infrastructure.cache

import com.github.benmanes.caffeine.cache.Caffeine
import org.hibernate.validator.constraints.ISBN
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.util.concurrent.TimeUnit

@Service
@Validated
class AmazonCoverCache {

  private val cache = Caffeine.newBuilder()
    .expireAfterAccess(2, TimeUnit.HOURS)
    .build<String, String>()

  fun getIfPresent(@ISBN isbn: String): String? = cache.getIfPresent(isbn)

  fun put(@ISBN isbn: String, cover: String?) = cache.put(isbn, cover.orEmpty())

  operator fun get(isbn: String): String? = getIfPresent(isbn)

  operator fun set(isbn: String, cover: String?) = put(isbn, cover)
}