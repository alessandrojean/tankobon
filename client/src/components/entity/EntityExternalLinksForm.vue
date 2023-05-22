<script setup lang="ts">
import { PlusIcon } from '@heroicons/vue/20/solid'
import { helpers } from '@vuelidate/validators'
import useVuelidate from '@vuelidate/core'
import { FormExternalLink } from '@/types/tankobon-external-link'

export interface EntityExternalLinksFormProps {
  externalLinks: FormExternalLink[]
  disabled?: boolean
  loading?: boolean
  types: string[]
}

const props = withDefaults(defineProps<EntityExternalLinksFormProps>(), {
  disabled: false,
  loading: false,
})

const emit = defineEmits<{
  (e: 'update:externalLinks', externalLinks: FormExternalLink[]): void
}>()

const { externalLinks, types } = toRefs(props)

const { t, locale } = useI18n()

function getRepeatedIndexes(externalLinks: FormExternalLink[]) {
  const visited = {} as Record<string, number>
  const duplicates = [] as number[]

  for (const [index, externalLink] of externalLinks.entries()) {
    const key = `${externalLink.type}-${externalLink.url}`
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

function unique(externalLinks: FormExternalLink[]) {
  return getRepeatedIndexes(externalLinks).length === 0
}

const listFormatter = computed(() => new Intl.ListFormat(locale.value, {
  type: 'conjunction',
  style: 'long',
}))

const duplicateIndexes = computed(() => getRepeatedIndexes(externalLinks.value))

const duplicatePositions = computed(() => {
  return listFormatter.value.format(duplicateIndexes.value.map(i => String(i + 1)))
})

const rules = computed(() => {
  const messageUnique = helpers.withMessage(t('validation.unique'), unique)

  return {
    externalLinks: { messageUnique },
  }
})

const v$ = useVuelidate(rules, { externalLinks })

defineExpose({ v$ })

function handleTypePicked(type: string, i: number) {
  const copy = structuredClone(toRaw(externalLinks.value))
  copy[i].type = type

  emit('update:externalLinks', copy)
  v$.value.externalLinks.$touch()
}

function handleUrlChange(url: string, i: number) {
  const copy = structuredClone(toRaw(externalLinks.value))
  copy[i].url = url

  emit('update:externalLinks', copy)
  v$.value.externalLinks.$touch()
}

function handleRemoveExternalLink(i: number) {
  const copy = structuredClone(toRaw(externalLinks.value))
  copy.splice(i, 1)

  emit('update:externalLinks', copy)
  v$.value.externalLinks.$touch()
}

const container = ref<HTMLFieldSetElement>()

async function addExternalLink() {
  const copy = structuredClone(toRaw(externalLinks.value))
  copy.push({ type: 'null', url: '' })

  emit('update:externalLinks', copy)
  await nextTick()

  const externalLinkIndex = copy.length - 1
  const inputToFocus = container.value
    ?.querySelector<HTMLInputElement>(`#url-input-${externalLinkIndex}`)

  inputToFocus?.focus()
}

onMounted(() => v$.value.$touch())

const typeOptions = computed(() => ['null', ...types.value])
const usedTypes = computed(() => externalLinks.value.map(e => e.type))
</script>

<template>
  <fieldset ref="container" :disabled="disabled || loading">
    <div class="flex flex-col gap-6">
      <div>
        <Button
          size="small"
          :disabled="loading || externalLinks.length === types.length"
          @click="addExternalLink"
        >
          <PlusIcon class="w-5 h-5" />
          <span>{{ $t('common-actions.add') }}</span>
        </Button>
      </div>
      <Alert
        :show="v$.externalLinks.$invalid"
        type="error"
      >
        <p class="font-semibold">
          {{ v$.externalLinks.$errors?.[0]?.$message }}
        </p>
        <p>{{ $t('validation.repeated-positions', [duplicatePositions]) }}</p>
      </Alert>
      <ul class="flex flex-col gap-4">
        <li
          v-for="(externalLink, i) of externalLinks"
          :key="i"
        >
          <EntityExternalLinkFormCard
            :index="i"
            :url="externalLink.url"
            :type="externalLink.type"
            :types="typeOptions"
            :invalid="duplicateIndexes.includes(i)"
            :disabled="disabled || loading"
            :disabled-types="usedTypes"
            @update:url="handleUrlChange($event, i)"
            @update:type="handleTypePicked($event, i)"
            @click:remove="handleRemoveExternalLink(i)"
          />
        </li>
      </ul>
    </div>
  </fieldset>
</template>
