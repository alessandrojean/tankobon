package io.github.alessandrojean.tankobon.application.tasks

import io.github.alessandrojean.tankobon.infrastructure.search.LuceneEntity
import java.io.Serializable

const val HIGHEST_PRIORITY = 8
const val HIGH_PRIORITY = 6
const val DEFAULT_PRIORITY = 4
const val LOW_PRIORITY = 2
const val LOWEST_PRIORITY = 0

sealed class Task(priority: Int = DEFAULT_PRIORITY, val groupId: String? = null) : Serializable {
  abstract fun uniqueId(): String
  val priority = priority.coerceIn(0, 9)

  class RebuildIndex(val entities: Set<LuceneEntity>?, priority: Int = DEFAULT_PRIORITY) : Task(priority) {
    override fun uniqueId() = "REBUILD_INDEX"
    override fun toString(): String = "RebuildIndex(priority='$priority',entities='${entities?.map { it.type }}')"
  }
}
