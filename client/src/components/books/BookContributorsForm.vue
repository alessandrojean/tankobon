<script setup lang="ts">
import Draggable from 'vuedraggable'
import { PlusIcon } from '@heroicons/vue/20/solid'
import { helpers } from '@vuelidate/validators'
import useVuelidate from '@vuelidate/core'
import type { BookContributorCreateUpdate } from '@/types/tankobon-book'
import type { PersonEntity } from '@/types/tankobon-person'
import { createEmptyPaginatedResponse, getRelationship } from '@/utils/api'

export interface BookContributorsFormProps {
  contributors: BookContributorCreateUpdate[]
  disabled?: boolean
  loading?: boolean
}

const props = withDefaults(defineProps<BookContributorsFormProps>(), {
  disabled: false,
  loading: false,
})

const emit = defineEmits<{
  (e: 'update:contributors', contributors: BookContributorCreateUpdate[]): void
}>()

const { contributors } = toRefs(props)

const { t, locale } = useI18n()
const notificator = useToaster()
const libraryStore = useLibraryStore()
const libraryId = computed(() => libraryStore.library!.id)

const { data: contributorRoles } = useLibraryContributorRolesQuery({
  libraryId,
  sort: [{ property: 'name', direction: 'asc' }],
  unpaged: true,
  select: response => response.data,
  initialData: () => createEmptyPaginatedResponse(),
  onError: async (error) => {
    await notificator.failure({
      title: t('contributor-roles.fetch-failure'),
      body: error.message,
    })
  },
})

const { data: people } = useLibraryPeopleQuery({
  libraryId,
  includes: ['person_picture'],
  sort: [{ property: 'name', direction: 'asc' }],
  unpaged: true,
  select: response => response.data,
  initialData: () => createEmptyPaginatedResponse(),
  onError: async (error) => {
    await notificator.failure({
      title: t('people.fetch-failure'),
      body: error.message,
    })
  },
})

const contributorRolesMap = computed(() => {
  return Object.fromEntries(
    contributorRoles.value!.map(c => [c.id, c]),
  )
})

const peopleMap = computed(() => {
  return Object.fromEntries(
    people.value!.map(p => [p.id, p]),
  )
})

function getPersonPicture(person: PersonEntity) {
  return getRelationship(person, 'PERSON_PICTURE')
}

function getRepeatedIndexes(contributors: BookContributorCreateUpdate[]) {
  const visited = {} as Record<string, number>
  const duplicates = [] as number[]

  for (const [index, contributor] of contributors.entries()) {
    const key = `${contributor.person}-${contributor.role}`
    if (visited[key] !== undefined) {
      const firstIndex = visited[key]

      if (firstIndex !== index && !duplicates.includes(firstIndex)) {
        duplicates.push(firstIndex)
      }

      duplicates.push(index)
    } else {
      visited[key] = index
    }
  }

  return duplicates
}

function unique(contributors: BookContributorCreateUpdate[]) {
  return getRepeatedIndexes(contributors).length === 0
}

const listFormatter = computed(() => new Intl.ListFormat(locale.value, {
  type: 'conjunction',
  style: 'long',
}))

const duplicateIndexes = computed(() => getRepeatedIndexes(contributors.value))

const duplicatePositions = computed(() => {
  return listFormatter.value.format(duplicateIndexes.value.map(i => String(i + 1)))
})

const rules = computed(() => {
  const messageUnique = helpers.withMessage(t('validation.unique'), unique)

  return {
    contributors: { messageUnique },
  }
})

const v$ = useVuelidate(rules, { contributors })

defineExpose({ v$ })

function handlePersonPicked(personId: string, i: number) {
  const copy = structuredClone(toRaw(contributors.value))
  copy[i].person = personId

  emit('update:contributors', copy)
  v$.value.contributors.$touch()
}

function handleRolePicked(roleId: string, i: number) {
  const copy = structuredClone(toRaw(contributors.value))
  copy[i].role = roleId

  emit('update:contributors', copy)
  v$.value.contributors.$touch()
}

function handleRemoveContributor(i: number) {
  const copy = structuredClone(toRaw(contributors.value))
  copy.splice(i, 1)

  emit('update:contributors', copy)
  v$.value.contributors.$touch()
}

const container = ref<HTMLFieldSetElement>()

async function addContributor() {
  const copy = structuredClone(toRaw(contributors.value))
  copy.push({ person: '', role: '' })

  emit('update:contributors', copy)
  await nextTick()

  const personIndex = copy.length - 1
  const inputToFocus = container.value
    ?.querySelector<HTMLInputElement>(`#person-input-${personIndex} input`)

  inputToFocus?.focus()
}

function handleDragAndDrop(newOrder: BookContributorCreateUpdate[]) {
  const copy: BookContributorCreateUpdate[] = JSON.parse(JSON.stringify(newOrder))
  emit('update:contributors', copy)
  v$.value.contributors.$touch()
}

onMounted(() => v$.value.$touch())

const personRoleMap = computed(() => {
  return contributors.value.reduce((map, contributor) => {
    if (contributor.person.length > 0 && contributor.role.length > 0) {
      if (map[contributor.person] !== undefined) {
        map[contributor.person].push(contributor.role)
      } else {
        map[contributor.person] = [contributor.role]
      }
    }

    return map
  }, {} as Record<string, string[]>)
})
</script>

<template>
  <fieldset ref="container" :disabled="disabled || loading">
    <div
      v-if="people!.length > 0 && contributorRoles!.length > 0"
      class="flex flex-col gap-6"
    >
      <div>
        <Button
          size="small"
          :disabled="loading"
          @click="addContributor"
        >
          <PlusIcon class="w-5 h-5" />
          <span>{{ $t('common-actions.add') }}</span>
        </Button>
      </div>
      <Alert
        :show="v$.contributors.$invalid"
        type="error"
      >
        <p class="font-semibold">
          {{ v$.contributors.$errors?.[0]?.$message }}
        </p>
        <p>{{ $t('validation.repeated-positions', [duplicatePositions]) }}</p>
      </Alert>
      <Draggable
        v-if="!loading && contributors.length > 0"
        class="flex flex-col gap-4"
        ghost-class="opacity-50"
        drag-class="cursor-grabbing"
        handle=".grabber"
        :model-value="contributors"
        :item-key="(c: BookContributorCreateUpdate) => `${c.person}-${c.role}`"
        :disabled="contributors.length === 1"
        @update:model-value="handleDragAndDrop"
      >
        <template #item="{ element: contributor, index: i }">
          <BookContributorFormCard
            :draggable="contributors.length > 1"
            :index="i"
            :role="contributorRolesMap[contributor.role]"
            :person="peopleMap[contributor.person]"
            :person-picture="getPersonPicture(peopleMap[contributor.person])?.attributes"
            :roles="contributorRoles ?? []"
            :people="people ?? []"
            :person-role-map="personRoleMap"
            :invalid="duplicateIndexes.includes(i)"
            @update:person="handlePersonPicked($event, i)"
            @update:role="handleRolePicked($event, i)"
            @click:remove="handleRemoveContributor(i)"
          />
        </template>
      </Draggable>
    </div>
  </fieldset>
</template>
