package io.github.alessandrojean.tankobon.infrastructure.jooq

import java.time.LocalDateTime
import java.time.ZoneId

fun LocalDateTime.toCurrentTimeZone(): LocalDateTime =
  this.atZone(ZoneId.of("Z"))
    .withZoneSameInstant(ZoneId.systemDefault())
    .toLocalDateTime()
