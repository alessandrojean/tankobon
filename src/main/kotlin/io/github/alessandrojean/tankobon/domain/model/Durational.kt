package io.github.alessandrojean.tankobon.domain.model

import java.time.LocalDateTime

interface Durational {
  val startedAt: LocalDateTime?
  val finishedAt: LocalDateTime?
}
