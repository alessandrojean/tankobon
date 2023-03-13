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
import io.github.alessandrojean.tankobon.infrastructure.image.UserAvatarLifecycle
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.EntityAttributesDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.EntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationshipType
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
) {

  private val expansionMap: Map<RelationshipType, IdsToAttributesFn> = mapOf(
    RelationshipType.COLLECTION to { ids ->
      collectionRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    RelationshipType.LIBRARY to { ids ->
      libraryRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    RelationshipType.USER to { ids ->
      userRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    RelationshipType.OWNER to { ids ->
      userRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    RelationshipType.LIBRARY_SHARING to { ids ->
      userRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    RelationshipType.PUBLISHER to { ids ->
      publisherRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    RelationshipType.TAG to { ids ->
      tagRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    RelationshipType.SERIES to { ids ->
      seriesRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    RelationshipType.STORE to { ids ->
      storeRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    RelationshipType.BOOK to { ids ->
      bookRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    RelationshipType.CONTRIBUTOR_ROLE to { ids ->
      contributorRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    RelationshipType.PERSON to { ids ->
      personRepository.findAllByIds(ids).associate { it.id to it.toAttributesDto() }
    },
    RelationshipType.CONTRIBUTOR to { ids ->
      bookContributorRepository.findAllByIdsAsDto(ids).associate { it.id to it.attributes }
    },
    RelationshipType.COVER_ART to { ids ->
      ids.associateWith { bookCoverLifecycle.getCoverDetails(it)!!.toAttributesDto() }
    },
    RelationshipType.AVATAR to { ids ->
      ids.associateWith { userAvatarLifecycle.getAvatarDetails(it)!!.toAttributesDto() }
    }
  )

  fun <T : EntityDto> expand(entity: T, relationsToExpand: Set<RelationshipType>): T {
    if (relationsToExpand.isEmpty()) {
      return entity
    }

    val relationsAttributes = entity.relationships.orEmpty()
      .filter { it.type in relationsToExpand }
      .groupBy({ it.type }, { it.id })
      .mapValues { (type, ids) -> expansionMap[type]!!.invoke(ids) }

    val relationships = entity.relationships?.map { relation ->
      relation.copy(attributes = relationsAttributes[relation.type]?.get(relation.id))
    }

    return entity.apply { this.relationships = relationships }
  }

  fun <T : EntityDto> expand(entities: Collection<T>, relationsToExpand: Set<RelationshipType>): List<T> {
    if (relationsToExpand.isEmpty()) {
      return entities.toList()
    }

    val relationsAttributes = entities
      .flatMap { it.relationships.orEmpty() }
      .distinctBy { it.id }
      .filter { it.type in relationsToExpand }
      .groupBy({ it.type }, { it.id })
      .mapValues { (type, ids) -> expansionMap[type]!!.invoke(ids) }

    return entities.map { entity ->
      val relationships = entity.relationships?.map { relation ->
        relation.copy(attributes = relationsAttributes[relation.type]?.get(relation.id))
      }

      entity.apply { this.relationships = relationships }
    }
  }
}
