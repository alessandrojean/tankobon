package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.jooq.Tables
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.SortField
import org.jooq.impl.DSL
import org.jooq.impl.DSL.falseCondition
import org.jooq.impl.DSL.noCondition
import org.springframework.data.domain.Sort
import java.time.LocalDateTime
import java.time.ZoneId

fun LocalDateTime.toCurrentTimeZone(): LocalDateTime =
  this.atZone(ZoneId.of("Z"))
    .withZoneSameInstant(ZoneId.systemDefault())
    .toLocalDateTime()

/**
 * Converts a [LocalDateTime] in the current system time zone
 * to a UTC date, casting it back to [LocalDateTime].
 *
 * Example:
 *
 * ```
 * // Now is by default at the system time zone (ZoneId.systemDefault()).
 * val now = LocalDateTime.now()
 * // Utc is now at ZoneId.UTC.
 * val utc = now.toUtc()
 * ```
 */
fun LocalDateTime.toUtcTimeZone(): LocalDateTime =
  this.atZone(ZoneId.systemDefault())
    .withZoneSameInstant(ZoneId.of("Z"))
    .toLocalDateTime()

fun Sort.toOrderBy(sorts: Map<String, Field<out Any>>): List<SortField<out Any>> =
  mapNotNull { it.toSortField(sorts) }

fun Sort.Order.toSortField(sorts: Map<String, Field<out Any>>): SortField<out Any>? {
  val f = sorts[property] ?: return null
  return if (isAscending) f.asc() else f.desc()
}

fun Field<String>.sortByValues(values: List<String>, asc: Boolean = true): Field<Int> {
  var c = DSL.choose(this).`when`("dummy sql", Int.MAX_VALUE)
  val multiplier = if (asc) 1 else -1
  values.forEachIndexed { index, value -> c = c.`when`(value, index * multiplier) }
  return c.otherwise(Int.MAX_VALUE)
}

fun Field<String>.inOrNoCondition(list: Collection<String>?): Condition = when {
  list == null -> noCondition()
  list.isEmpty() -> falseCondition()
  else -> this.`in`(list)
}

fun DSLContext.insertTempStrings(batchSize: Int, collection: Collection<String>) {
  deleteFrom(Tables.TEMP_STRING_LIST).execute()

  if (collection.isNotEmpty()) {
    collection.chunked(batchSize).forEach { chunk ->
      batch(insertInto(Tables.TEMP_STRING_LIST, Tables.TEMP_STRING_LIST.STRING).values(null as String?))
        .also { step -> chunk.forEach { step.bind(it) } }
        .execute()
    }
  }
}

fun DSLContext.selectTempStrings() =
  select(Tables.TEMP_STRING_LIST.STRING)
    .from(Tables.TEMP_STRING_LIST)