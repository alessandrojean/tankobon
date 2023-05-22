package io.github.alessandrojean.tankobon.interfaces.scheduler

import io.github.alessandrojean.tankobon.application.tasks.HIGHEST_PRIORITY
import io.github.alessandrojean.tankobon.application.tasks.TaskEmitter
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneHelper
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Profile("!test")
@Component
class SearchIndexController(
  private val luceneHelper: LuceneHelper,
  private val taskEmitter: TaskEmitter,
) {

  @EventListener(ApplicationReadyEvent::class)
  fun createIndexIfNoneExist() {
    if (!luceneHelper.indexExists()) {
      logger.info { "Lucene index not found, trigger rebuild" }
      taskEmitter.rebuildIndex(HIGHEST_PRIORITY)
    } else {
      logger.info { "Lucene index version: ${luceneHelper.getIndexVersion()}" }
      when (luceneHelper.getIndexVersion()) {
        1 -> taskEmitter.rebuildIndex(HIGHEST_PRIORITY)
      }
    }
  }
}
