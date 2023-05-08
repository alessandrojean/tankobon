package io.github.alessandrojean.tankobon.application.tasks

import io.github.alessandrojean.tankobon.infrastructure.jms.QUEUE_FACTORY
import io.github.alessandrojean.tankobon.infrastructure.jms.QUEUE_TASKS
import io.github.alessandrojean.tankobon.infrastructure.search.SearchIndexLifecycle
import io.github.alessandrojean.tankobon.interfaces.scheduler.METER_TASKS_EXECUTION
import io.github.alessandrojean.tankobon.interfaces.scheduler.METER_TASKS_FAILURE
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import kotlin.time.measureTime
import kotlin.time.toJavaDuration

private val logger = KotlinLogging.logger {}

@Service
class TaskHandler(
  private val taskEmitter: TaskEmitter,
  private val searchIndexLifecycle: SearchIndexLifecycle,
  private val meterRegistry: MeterRegistry,
) {

  @JmsListener(
    destination = QUEUE_TASKS,
    containerFactory = QUEUE_FACTORY,
    concurrency = "#{@tankobonProperties.taskConsumers}-#{@tankobonProperties.taskConsumersMax}"
  )
  fun handleTask(task: Task) {
    logger.info { "Executing task: $task" }
    try {
      val duration = measureTime {
        when (task) {
          is Task.RebuildIndex -> searchIndexLifecycle.rebuildIndex(task.entities)
        }
      }

      logger.info { "Task $task executed in $duration" }
      meterRegistry
        .timer(METER_TASKS_EXECUTION, "type", task.javaClass.simpleName)
        .record(duration.toJavaDuration())
    } catch (e: Exception) {
      logger.error(e) { "Task $task execution failed" }
      meterRegistry
        .counter(METER_TASKS_FAILURE, "type", task.javaClass.simpleName)
        .increment()
    }
  }
}