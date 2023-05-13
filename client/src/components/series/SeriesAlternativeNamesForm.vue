<script setup lang="ts">
import Draggable from 'vuedraggable'
import { PlusIcon } from '@heroicons/vue/20/solid'
import { helpers } from '@vuelidate/validators'
import useVuelidate from '@vuelidate/core'
import { v4 as uuid } from 'uuid'
import type { AlternativeName, FormAlternativeName } from '@/types/tankobon-alternative-name'
import { BCP47_OPTIONS } from '@/utils/language'

export interface SeriesAlternativeNamesFormProps {
  alternativeNames: FormAlternativeName[]
  disabled?: boolean
  loading?: boolean
}

const props = withDefaults(defineProps<SeriesAlternativeNamesFormProps>(), {
  disabled: false,
  loading: false,
})

const emit = defineEmits<{
  (e: 'update:alternativeNames', alternativeNames: FormAlternativeName[]): void
}>()

const { alternativeNames } = toRefs(props)

const { t, locale } = useI18n()

function getRepeatedIndexes(alternativeNames: AlternativeName[]) {
  const visited = {} as Record<string, number>
  const duplicates = [] as number[]

  for (const [index, alternativeName] of alternativeNames.entries()) {
    const key = `${alternativeName.language}-${alternativeName.name}`
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

function unique(alternativeNames: AlternativeName[]) {
  return getRepeatedIndexes(alternativeNames).length === 0
}

const listFormatter = computed(() => new Intl.ListFormat(locale.value, {
  type: 'conjunction',
  style: 'long',
}))

const duplicateIndexes = computed(() => getRepeatedIndexes(alternativeNames.value))

const duplicatePositions = computed(() => {
  return listFormatter.value.format(duplicateIndexes.value.map(i => String(i + 1)))
})

const rules = computed(() => {
  const messageUnique = helpers.withMessage(t('validation.unique'), unique)

  return {
    alternativeNames: { messageUnique },
  }
})

const v$ = useVuelidate(rules, { alternativeNames })

defineExpose({ v$ })

function handleLanguagePicked(language: string, i: number) {
  const copy = structuredClone(toRaw(alternativeNames.value))
  copy[i].language = language

  emit('update:alternativeNames', copy)
  v$.value.alternativeNames.$touch()
}

function handleNameChange(name: string, i: number) {
  const copy = structuredClone(toRaw(alternativeNames.value))
  copy[i].name = name

  emit('update:alternativeNames', copy)
  v$.value.alternativeNames.$touch()
}

function handleRemoveAlternativeName(i: number) {
  const copy = structuredClone(toRaw(alternativeNames.value))
  copy.splice(i, 1)

  emit('update:alternativeNames', copy)
  v$.value.alternativeNames.$touch()
}

const container = ref<HTMLFieldSetElement>()

async function addAlternativeName() {
  const copy = structuredClone(toRaw(alternativeNames.value))
  copy.push({ id: uuid(), language: 'null', name: '' })

  emit('update:alternativeNames', copy)
  await nextTick()

  const alternativeNameIndex = copy.length - 1
  const inputToFocus = container.value
    ?.querySelector<HTMLInputElement>(`#name-input-${alternativeNameIndex}`)

  inputToFocus?.focus()
}

function handleDragAndDrop(newOrder: AlternativeName[]) {
  const copy: FormAlternativeName[] = JSON.parse(JSON.stringify(newOrder))
  emit('update:alternativeNames', copy)
  v$.value.alternativeNames.$touch()
}

onMounted(() => v$.value.$touch())

const languageOptions = ['null', ...BCP47_OPTIONS]
</script>

<template>
  <fieldset ref="container" :disabled="disabled || loading">
    <div class="flex flex-col gap-6">
      <div>
        <Button
          size="small"
          :disabled="loading"
          @click="addAlternativeName"
        >
          <PlusIcon class="w-5 h-5" />
          <span>{{ $t('common-actions.add') }}</span>
        </Button>
      </div>
      <Alert
        :show="v$.alternativeNames.$invalid"
        type="error"
      >
        <p class="font-semibold">
          {{ v$.alternativeNames.$errors?.[0]?.$message }}
        </p>
        <p>{{ $t('validation.repeated-positions', [duplicatePositions]) }}</p>
      </Alert>
      <Draggable
        v-if="!loading && alternativeNames.length > 0"
        class="flex flex-col gap-4"
        ghost-class="opacity-50"
        drag-class="cursor-grabbing"
        handle=".grabber"
        item-key="id"
        :model-value="alternativeNames"
        :disabled="alternativeNames.length === 1"
        @update:model-value="handleDragAndDrop"
      >
        <template #item="{ element: alternativeName, index: i }">
          <SeriesAlternativeNameFormCard
            :draggable="alternativeNames.length > 1"
            :index="i"
            :name="alternativeName.name"
            :language="alternativeName.language"
            :languages="languageOptions"
            :invalid="duplicateIndexes.includes(i)"
            :disabled="disabled || loading"
            @update:name="handleNameChange($event, i)"
            @update:language="handleLanguagePicked($event, i)"
            @click:remove="handleRemoveAlternativeName(i)"
          />
        </template>
      </Draggable>
    </div>
  </fieldset>
</template>
