<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import SeriesForm from '@/components/series/SeriesForm.vue'
import type { SeriesCreation } from '@/types/tankobon-series'

export interface SeriesCreateDialogProps {
  libraryId: string
  isOpen: boolean
}

export interface SeriesCreateDialogEmits {
  (e: 'close'): void
  (e: 'submit', series: SeriesCreation): void
}

const props = defineProps<SeriesCreateDialogProps>()
const emit = defineEmits<SeriesCreateDialogEmits>()

const { isOpen, libraryId } = toRefs(props)

const series = reactive<SeriesCreation>({
  name: '',
  description: '',
  library: '',
})

const seriesForm = ref<InstanceType<typeof SeriesForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(series, {
    name: '',
    description: '',
    library: libraryId.value,
  })

  seriesForm.value?.v$.$reset()
})

async function handleSubmit() {
  const isValid = await seriesForm.value!.v$.$validate()

  if (!isValid) {
    return
  }

  emit('close')
  emit('submit', toRaw(series))
}
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-2xl"
    novalidate
    :is-open="isOpen"
    :title="$t('series.create-header')"
    :description="$t('series.create-description')"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <SeriesForm
        ref="seriesForm"
        v-model:name="series.name"
        v-model:description="series.description"
      />
    </template>
    <template #footer>
      <Button
        type="submit"
        class="ml-auto"
        kind="primary"
      >
        <CheckIcon class="w-5 h-5" />
        <span>{{ $t('common-actions.create') }}</span>
      </Button>
    </template>
  </Dialog>
</template>
