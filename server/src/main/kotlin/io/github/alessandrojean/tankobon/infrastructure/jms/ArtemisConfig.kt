package io.github.alessandrojean.tankobon.infrastructure.jms

import jakarta.jms.ConnectionFactory
import mu.KotlinLogging
import org.apache.activemq.artemis.api.core.QueueConfiguration
import org.apache.activemq.artemis.api.core.RoutingType
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.apache.activemq.artemis.core.config.Configuration as ArtemisConfiguration

private val logger = KotlinLogging.logger {}

const val TOPIC_EVENTS = "domain.events"
const val JMS_PROPERTY_TYPE = "type"
const val TOPIC_FACTORY = "topicJmsListenerContainerFactory"

@Configuration
class ArtemisConfig : ArtemisConfigurationCustomizer {

  override fun customize(configuration: ArtemisConfiguration?) {
    configuration?.let {
      it.maxDiskUsage = 100
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

}