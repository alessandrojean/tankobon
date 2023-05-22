package io.github.alessandrojean.tankobon.domain.service

import io.github.alessandrojean.tankobon.domain.persistence.BookContributorRepository
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.domain.persistence.CollectionRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.PersonRepository
import io.github.alessandrojean.tankobon.domain.persistence.PublisherRepository
import io.github.alessandrojean.tankobon.domain.persistence.SeriesRepository
import io.github.alessandrojean.tankobon.domain.persistence.StoreRepository
import io.github.alessandrojean.tankobon.domain.persistence.TagRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.infrastructure.image.BookCoverLifecycle
import io.github.alessandrojean.tankobon.infrastructure.image.PersonPictureLifecycle
import io.github.alessandrojean.tankobon.infrastructure.image.PublisherPictureLifecycle
import io.github.alessandrojean.tankobon.infrastructure.image.SeriesCoverLifecycle
import io.github.alessandrojean.tankobon.infrastructure.image.UserAvatarLifecycle
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.EntityAttributesDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.EntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ReferenceExpansionEnum
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toAttributesDto
import org.springframework.stereotype.Component

private typealias IdsToAttributesFn = (List<String>) -> Map<String, EntityAttributesDto>

@Component
class ReferenceExpansion(
  private val collectionRepository: CollectionRepository,
  private val libraryRepository: LibraryRepository,
  private val userRepository: TankobonUserRepository,
  private val publisherRepository: PublisherRepository,
  private val tagRepository: TagRepository,
  private val seriesRepository: SeriesRepository,
  private val storeRepository: StoreRepository,
  private val bookContributorRepository: BookContributorRepository,
  private val bookRepository: BookRepository,
  private val contributorRepository: BookContributorRepository,
  private val personRepository: PersonRepository,
  private val bookCoverLifecycle: BookCoverLifecycle,
  private val userAvatarLifecycle: UserAvatarLifecycle,
  private val personPictureLifecycle: PersonPictureLifecycle,
  private val publisherPictureLifecycle: PublisherPictureLifecycle,
  private val seriesCoverLifecycle: SeriesCoverLifecycle,
) {

  private val expansionMap: Map<String, IdsToAttributesFn> = mapOf(
    "COLLECTION" to { ids ->
      collectionRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    "LIBRARY" to { ids ->
      libraryRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    "USER" to { ids ->
      userRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    "OWNER" to { ids ->
      userRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    "LIBRARY_SHARING" to { ids ->
      userRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    "PUBLISHER" to { ids ->
      publisherRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    "TAG" to { ids ->
      tagRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    "SERIES" to { ids ->
      val alternativeNames = seriesRepository.findAlternativeNamesByIds(ids)
      seriesRepository.findAllByIds(ids).associate {
        it.id to it.toAttributesDto(alternativeNames[it.id].orEmpty())
      }
    },
    "STORE" to { ids ->
      storeRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    "BOOK" to { ids ->
      bookRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    "PREVIOUS_BOOK" to { ids ->
      bookRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    "NEXT_BOOK" to { ids ->
      bookRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    "CONTRIBUTOR_ROLE" to { ids ->
      contributorRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    "PERSON" to { ids ->
      personRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    "CONTRIBUTOR" to { ids ->
      bookContributorRepository.findAllByIdsAsDto(ids).associate { it.id to it.attributes }
    },
    "COVER_ART" to { ids ->
      ids.associateWith { bookCoverLifecycle.getImageDetails(it)!!.toAttributesDto() }
    },
    "AVATAR" to { ids ->
      ids.associateWith { userAvatarLifecycle.getImageDetails(it)!!.toAttributesDto() }
    },
    "PERSON_PICTURE" to { ids ->
      ids.associateWith { personPictureLifecycle.getImageDetails(it)!!.toAttributesDto() }
    },
    "PUBLISHER_PICTURE" to { ids ->
      ids.associateWith { publisherPictureLifecycle.getImageDetails(it)!!.toAttributesDto() }
    },
    "SERIES_COVER" to { ids ->
      ids.associateWith { seriesCoverLifecycle.getImageDetails(it)!!.toAttributesDto() }
    },
  )

  fun <R : ReferenceExpansionEnum, T : EntityDto<R>> expand(entity: T, relationsToExpand: Set<R>): T {
    if (relationsToExpand.isEmpty()) {
      return entity
    }

    val relationsAttributes = entity.relationships.orEmpty()
      .filter { it.type in relationsToExpand }
      .groupBy({ it.type.toString() }, { it.id })
      .mapValues { (type, ids) -> expansionMap[type]!!.invoke(ids) }

    val relationships = entity.relationships?.map { relation ->
      relation.copy(attributes = relationsAttributes[relation.type.toString()]?.get(relation.id))
    }

    return entity.apply { this.relationships = relationships }
  }

  fun <R : ReferenceExpansionEnum, T : EntityDto<R>> expand(entities: Collection<T>, relationsToExpand: Set<R>): List<T> {
    if (relationsToExpand.isEmpty()) {
      return entities.toList()
    }

    val relationsAttributes = entities
      .flatMap { it.relationships.orEmpty() }
      .distinctBy { "${it.type}-${it.id}" }
      .filter { it.type in relationsToExpand }
      .groupBy({ it.type.toString() }, { it.id })
      .mapValues { (type, ids) -> expansionMap[type]!!.invoke(ids) }

    return entities.map { entity ->
      val relationships = entity.relationships?.map { relation ->
        relation.copy(attributes = relationsAttributes[relation.type.toString()]?.get(relation.id))
      }

      entity.apply { this.relationships = relationships }
    }
  }
}
