package io.github.alessandrojean.tankobon.infrastructure.search

import io.github.alessandrojean.tankobon.domain.model.BookSearch
import io.github.alessandrojean.tankobon.domain.model.Collection
import io.github.alessandrojean.tankobon.domain.model.CollectionSearch
import io.github.alessandrojean.tankobon.domain.model.ContributorRole
import io.github.alessandrojean.tankobon.domain.model.ContributorRoleSearch
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.Person
import io.github.alessandrojean.tankobon.domain.model.PersonSearch
import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.github.alessandrojean.tankobon.domain.model.PublisherSearch
import io.github.alessandrojean.tankobon.domain.model.Series
import io.github.alessandrojean.tankobon.domain.model.SeriesSearch
import io.github.alessandrojean.tankobon.domain.model.Store
import io.github.alessandrojean.tankobon.domain.model.StoreSearch
import io.github.alessandrojean.tankobon.domain.model.Tag
import io.github.alessandrojean.tankobon.domain.model.TagSearch
import io.github.alessandrojean.tankobon.domain.persistence.CollectionRepository
import io.github.alessandrojean.tankobon.domain.persistence.ContributorRoleRepository
import io.github.alessandrojean.tankobon.domain.persistence.PersonRepository
import io.github.alessandrojean.tankobon.domain.persistence.PublisherRepository
import io.github.alessandrojean.tankobon.domain.persistence.SeriesRepository
import io.github.alessandrojean.tankobon.domain.persistence.StoreRepository
import io.github.alessandrojean.tankobon.domain.persistence.TagRepository
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.infrastructure.jms.TOPIC_EVENTS
import io.github.alessandrojean.tankobon.infrastructure.jms.TOPIC_FACTORY
import io.github.alessandrojean.tankobon.interfaces.api.persistence.BookDtoRepository
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ReferenceExpansionBook
import mu.KotlinLogging
import org.apache.lucene.document.Document
import org.apache.lucene.index.Term
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import kotlin.math.ceil
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}
private const val INDEX_VERSION = 1

@Component
class SearchIndexLifecycle(
  private val collectionRepository: CollectionRepository,
  private val bookDtoRepository: BookDtoRepository,
  private val seriesRepository: SeriesRepository,
  private val publisherRepository: PublisherRepository,
  private val personRepository: PersonRepository,
  private val tagRepository: TagRepository,
  private val contributorRoleRepository: ContributorRoleRepository,
  private val storeRepository: StoreRepository,
  private val luceneHelper: LuceneHelper,
  private val referenceExpansion: ReferenceExpansion,
) {

  fun rebuildIndex(entities: Set<LuceneEntity>? = null) {
    val targetEntities = entities ?: LuceneEntity.values().toSet()

    logger.info { "Rebuild search index for: ${targetEntities.map(LuceneEntity::type)}" }

    targetEntities.forEach {
      when (it) {
        LuceneEntity.Book -> rebuildIndex(
          entity = it,
          { p: Pageable -> bookDtoRepository.findAll(BookSearch(), p).expand() },
          { e: BookEntityDto -> e.toDocument() }
        )
        LuceneEntity.Series -> rebuildIndex(
          entity = it,
          { p: Pageable -> seriesRepository.findAll(SeriesSearch(), p) },
          { e: Series -> e.toDocument() }
        )
        LuceneEntity.Collection -> rebuildIndex(
          entity = it,
          { p: Pageable -> collectionRepository.findAll(CollectionSearch(), p) },
          { e: Collection -> e.toDocument() }
        )
        LuceneEntity.Publisher -> rebuildIndex(
          entity = it,
          { p: Pageable -> publisherRepository.findAll(PublisherSearch(), p) },
          { e: Publisher -> e.toDocument() }
        )
        LuceneEntity.Person -> rebuildIndex(
          entity = it,
          { p: Pageable -> personRepository.findAll(PersonSearch(), p) },
          { e: Person -> e.toDocument() }
        )
        LuceneEntity.Tag -> rebuildIndex(
          entity = it,
          { p: Pageable -> tagRepository.findAll(TagSearch(), p) },
          { e: Tag -> e.toDocument() }
        )
        LuceneEntity.ContributorRole -> rebuildIndex(
          entity = it,
          { p: Pageable -> contributorRoleRepository.findAll(ContributorRoleSearch(), p) },
          { e: ContributorRole -> e.toDocument() }
        )
        LuceneEntity.Store -> rebuildIndex(
          entity = it,
          { p: Pageable -> storeRepository.findAll(StoreSearch(), p) },
          { e: Store -> e.toDocument() }
        )
      }
    }
  }

  private fun <T> rebuildIndex(
    entity: LuceneEntity,
    provider: (Pageable) -> Page<out T>,
    toDoc: (T) -> Document
  ) {
    logger.info { "Rebuilding index for ${entity.name}" }

    val count = provider(Pageable.ofSize(1)).totalElements
    val batchSize = 5_000
    val pages = ceil(count.toDouble() / batchSize).toInt()
    logger.info { "Number of entities: $count" }

    luceneHelper.getIndexWriter().use { indexWriter ->
      val duration = measureTime {
        indexWriter.deleteDocuments(Term(LuceneEntity.TYPE, entity.type))

        (0 until pages).forEach { page ->
          logger.info { "Processing page ${page + 1} of $pages ($batchSize elements)" }
          val entityDocs = provider(PageRequest.of(page, batchSize))
            .content.map { toDoc(it) }

          indexWriter.addDocuments(entityDocs)
        }
      }

      logger.info { "Wrote ${entity.name} index in $duration" }
    }

    luceneHelper.setIndexVersion(INDEX_VERSION)
    logger.info { "Lucene search index version: ${luceneHelper.getIndexVersion()}" }
  }

  @JmsListener(destination = TOPIC_EVENTS, containerFactory = TOPIC_FACTORY)
  fun consumeEvents(event: DomainEvent) {
    when (event) {
      is DomainEvent.SeriesAdded ->
        seriesRepository.findByIdOrNull(event.series.id)
          ?.toDocument()?.let { addEntity(it) }
      is DomainEvent.SeriesUpdated ->
        seriesRepository.findByIdOrNull(event.series.id)
          ?.toDocument()?.let { updateEntity(LuceneEntity.Series, event.series.id, it) }
      is DomainEvent.SeriesDeleted -> deleteEntity(LuceneEntity.Series, event.series.id)

      is DomainEvent.BookAdded ->
        bookDtoRepository.findByIdOrNull(event.book.id)?.expand()
          ?.toDocument()
          ?.let { addEntity(it) }
      is DomainEvent.BookUpdated ->
        bookDtoRepository.findByIdOrNull(event.book.id)?.expand()
          ?.toDocument()
          ?.let { updateEntity(LuceneEntity.Book, event.book.id, it) }
      is DomainEvent.BookDeleted -> deleteEntity(LuceneEntity.Book, event.book.id)

      is DomainEvent.CollectionAdded ->
        collectionRepository.findByIdOrNull(event.collection.id)
          ?.toDocument()?.let { addEntity(it) }
      is DomainEvent.CollectionUpdated ->
        collectionRepository.findByIdOrNull(event.collection.id)
          ?.toDocument()?.let { updateEntity(LuceneEntity.Collection, event.collection.id, it) }
      is DomainEvent.CollectionDeleted -> deleteEntity(LuceneEntity.Collection, event.collection.id)

      is DomainEvent.PublisherAdded ->
        publisherRepository.findByIdOrNull(event.publisher.id)
          ?.toDocument()?.let { addEntity(it) }
      is DomainEvent.PublisherUpdated ->
        publisherRepository.findByIdOrNull(event.publisher.id)
          ?.toDocument()?.let { updateEntity(LuceneEntity.Publisher, event.publisher.id, it) }
      is DomainEvent.PublisherDeleted -> deleteEntity(LuceneEntity.Publisher, event.publisher.id)

      is DomainEvent.PersonAdded ->
        personRepository.findByIdOrNull(event.person.id)
          ?.toDocument()?.let { addEntity(it) }
      is DomainEvent.PersonUpdated ->
        personRepository.findByIdOrNull(event.person.id)
          ?.toDocument()?.let { updateEntity(LuceneEntity.Person, event.person.id, it) }
      is DomainEvent.PersonDeleted -> deleteEntity(LuceneEntity.Person, event.person.id)

      is DomainEvent.TagAdded ->
        tagRepository.findByIdOrNull(event.tag.id)
          ?.toDocument()?.let { addEntity(it) }
      is DomainEvent.TagUpdated ->
        tagRepository.findByIdOrNull(event.tag.id)
          ?.toDocument()?.let { updateEntity(LuceneEntity.Tag, event.tag.id, it) }
      is DomainEvent.TagDeleted -> deleteEntity(LuceneEntity.Tag, event.tag.id)

      is DomainEvent.ContributorRoleAdded ->
        contributorRoleRepository.findByIdOrNull(event.contributorRole.id)
          ?.toDocument()?.let { addEntity(it) }
      is DomainEvent.ContributorRoleUpdated ->
        contributorRoleRepository.findByIdOrNull(event.contributorRole.id)
          ?.toDocument()?.let { updateEntity(LuceneEntity.ContributorRole, event.contributorRole.id, it) }
      is DomainEvent.ContributorRoleDeleted -> deleteEntity(LuceneEntity.ContributorRole, event.contributorRole.id)

      is DomainEvent.StoreAdded ->
        storeRepository.findByIdOrNull(event.store.id)
          ?.toDocument()?.let { addEntity(it) }
      is DomainEvent.StoreUpdated ->
        storeRepository.findByIdOrNull(event.store.id)
          ?.toDocument()?.let { updateEntity(LuceneEntity.Store, event.store.id, it) }
      is DomainEvent.StoreDeleted -> deleteEntity(LuceneEntity.Store, event.store.id)

      else -> Unit
    }
  }

  private fun BookEntityDto.expand(): BookEntityDto = referenceExpansion.expand(
    entity = this,
    relationsToExpand = setOf(
      ReferenceExpansionBook.LIBRARY,
      ReferenceExpansionBook.TAG,
      ReferenceExpansionBook.PUBLISHER,
      ReferenceExpansionBook.CONTRIBUTOR
    )
  )

  private fun Page<BookEntityDto>.expand(): Page<BookEntityDto> {
    val expanded = referenceExpansion.expand(
      entities = content,
      relationsToExpand = setOf(
        ReferenceExpansionBook.LIBRARY,
        ReferenceExpansionBook.TAG,
        ReferenceExpansionBook.PUBLISHER,
        ReferenceExpansionBook.CONTRIBUTOR
      )
    )

    return PageImpl(
      expanded,
      PageRequest.of(pageable.pageNumber, pageable.pageSize),
      totalElements,
    )
  }

  private fun addEntity(doc: Document) {
    luceneHelper.getIndexWriter().use { indexWriter ->
      indexWriter.addDocument(doc)
    }
  }

  private fun updateEntity(entity: LuceneEntity, entityId: String, newDoc: Document) {
    luceneHelper.getIndexWriter().use { indexWriter ->
      indexWriter.updateDocument(Term(entity.id, entityId), newDoc)
    }
  }

  private fun deleteEntity(entity: LuceneEntity, entityId: String) {
    luceneHelper.getIndexWriter().use { indexWriter ->
      indexWriter.deleteDocuments(Term(entity.id, entityId))
    }
  }
}