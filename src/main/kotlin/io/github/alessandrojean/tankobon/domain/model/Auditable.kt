package io.github.alessandrojean.tankobon.domain.model

import java.time.LocalDateTime

interface Auditable {
  val createdAt: LocalDateTime
  val modifiedAt: LocalDateTime
}
