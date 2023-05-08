package io.github.alessandrojean.tankobon.infrastructure.jms

import jakarta.jms.ConnectionFactory
import mu.KotlinLogging
import org.apache.activemq.artemis.api.core.QueueConfiguration
import org.apache.activemq.artemis.api.core.RoutingType
import org.apache.activemq.artemis.core.settings.impl.AddressSettings
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.apache.activemq.artemis.core.config.Configuration as ArtemisConfiguration

private val logger = KotlinLogging.logger {}

const val QUEUE_UNIQUE_ID = "unique_id"

const val QUEUE_TASKS = "tasks.background"
const val TOPIC_EVENTS = "domain.events"

const val JMS_PROPERTY_TYPE = "type"

const val TOPIC_FACTORY = "topicJmsListenerContainerFactory"
const val QUEUE_FACTORY = "queueJmsListenerContainerFactory"

@Configuration
class ArtemisConfig : ArtemisConfigurationCustomizer {

  override fun customize(configuration: ArtemisConfiguration?) {
    configuration?.let {
      it.maxDiskUsage = 100
      it.addAddressSetting(
        QUEUE_TASKS,
        AddressSettings().apply { defaultConsumerWindowSize = 0 }
      )
      it.addQueueConfiguration(
        QueueConfiguration(QUEUE_TASKS)
          .setAddress(QUEUE_TASKS)
          .setLastValueKey(QUEUE_UNIQUE_ID)
          .setRoutingType(RoutingType.ANYCAST)
      )
      it.addQueueConfiguration(
        QueueConfiguration(TOPIC_EVENTS)
          .setAddress(TOPIC_EVENTS)
          .setRoutingType(RoutingType.MULTICAST)
      )
    }
  }

  @Bean(TOPIC_FACTORY)
  fun topicJmsListenerContainerFactory(
    connectionFactory: ConnectionFactory,
    configurer: DefaultJmsListenerContainerFactoryConfigurer,
  ): DefaultJmsListenerContainerFactory = DefaultJmsListenerContainerFactory().apply {
    configurer.configure(this, connectionFactory)
    setErrorHandler { logger.warn { it.message } }
    setPubSubDomain(true)
  }

  @Bean(QUEUE_FACTORY)
  fun queueJmsListenerContainerFactory(
    connectionFactory: ConnectionFactory,
    configurer: DefaultJmsListenerContainerFactoryConfigurer
  ): DefaultJmsListenerContainerFactory = DefaultJmsListenerContainerFactory().apply {
    configurer.configure(this, connectionFactory)
    setErrorHandler { logger.warn { it.message } }
  }

}
