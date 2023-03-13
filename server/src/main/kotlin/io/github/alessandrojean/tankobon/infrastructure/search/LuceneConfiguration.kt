package io.github.alessandrojean.tankobon.infrastructure.search

import io.github.alessandrojean.tankobon.infrastructure.configuration.TankobonProperties
import org.apache.lucene.store.ByteBuffersDirectory
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.nio.file.Paths

@Configuration
class LuceneConfiguration(
  private val tankobonProperties: TankobonProperties,
) {

  @Bean
  fun indexAnalyzer() = with(tankobonProperties.lucene.indexAnalyzer) {
    MultiLingualNGramAnalyzer(minGram, maxGram, preserveOriginal)
  }

  @Bean
  fun searchAnalyzer() = MultiLingualAnalyzer()

  @Bean
  @Profile("test")
  fun memoryDirectory(): Directory = ByteBuffersDirectory()

  @Bean
  @Profile("!test")
  fun diskDirectory(): Directory =
    FSDirectory.open(Paths.get(tankobonProperties.lucene.dataDirectory))
}
