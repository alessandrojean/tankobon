import { Entity, Relationship, RelationshipType } from '@/types/tankobon-entity'
import { ImageDetailsAttributes } from '@/types/tankobon-image-details'
import { LibraryAttributes } from '@/types/tankobon-library'
import { UserAttributes } from '@/types/tankobon-user'

type AttributeType<T extends RelationshipType> =
  T extends 'LIBRARY' ? LibraryAttributes :
  T extends 'LIBRARY_SHARING' ? LibraryAttributes :
  T extends 'AVATAR' ? ImageDetailsAttributes :
  T extends 'USER' ? UserAttributes :
  T extends 'OWNER' ? UserAttributes :
  unknown


export function getRelationship<T extends RelationshipType>(
  entity: Entity<any> | undefined | null,
  type: T
): Relationship<AttributeType<T>> | undefined {
  return entity?.relationships?.find((r) => r.type === type)
}
