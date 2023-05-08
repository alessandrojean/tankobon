package io.github.alessandrojean.tankobon.application.tasks

import io.github.alessandrojean.tankobon.infrastructure.jms.JMS_PROPERTY_TYPE
import io.github.alessandrojean.tankobon.infrastructure.jms.QUEUE_TASKS
import io.github.alessandrojean.tankobon.infrastructure.jms.QUEUE_UNIQUE_ID
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneEntity
import jakarta.jms.ConnectionFactory
import mu.KotlinLogging
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class TaskEmitter(
  connectionFactory: ConnectionFactory,
) {

  private val jmsTemplates = (0..9).associateWith {
    JmsTemplate(connectionFactory).apply {
      priority = it
      isExplicitQosEnabled = true
    }
  }

  fun rebuildIndex(priority: Int = DEFAULT_PRIORITY, entities: Set<LuceneEntity>? = null) {
    submitTask(Task.RebuildIndex(entities, priority))
  }

  private fun submitTask(task: Task) {
    logger.info { "Sending task: $task" }
    jmsTemplates[task.priority]!!.convertAndSend(QUEUE_TASKS, task) {
      it.apply {
        setStringProperty(QUEUE_UNIQUE_ID, task.uniqueId())
        setStringProperty(JMS_PROPERTY_TYPE, task.javaClass.simpleName)
        task.groupId?.let { groupId -> setStringProperty("JMSXGroupID", groupId) }
      }
    }
  }
}