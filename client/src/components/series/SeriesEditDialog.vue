<script lang="ts" setup>
import type { SeriesEntity, SeriesUpdate } from '@/types/tankobon-series'
import { CheckIcon } from '@heroicons/vue/20/solid'
import SeriesForm from '@/components/series/SeriesForm.vue'

export interface SeriesEditDialogProps {
  isOpen: boolean,
  seriesEntity: SeriesEntity,
}

export type SeriesEditDialogEmits = {
  (e: 'close'): void,
  (e: 'submit', series: SeriesUpdate): void,
}

const props = defineProps<SeriesEditDialogProps>()
const emit = defineEmits<SeriesEditDialogEmits>()

const { isOpen, seriesEntity } = toRefs(props)

const series = reactive<SeriesUpdate>({
  id: '',
  name: '',
  description: '',
})

const seriesForm = ref<InstanceType<typeof SeriesForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(series, {
    id: seriesEntity.value.id,
    name: seriesEntity.value.attributes.name,
    description: seriesEntity.value.attributes.description,
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
    dialog-class="max-w-md"
    novalidate
    :is-open="isOpen"
    :title="$t('series.update-header')"
    :description="$t('series.update-description', [seriesEntity.attributes.name])"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <SeriesForm
        ref="seriesForm"
        v-model:name="series.name"
        v-model:description="series.description"
        mode="update"
      />
    </template>
    <template #footer>
      <Button
        type="submit"
        class="ml-auto"
        kind="primary"
      >
        <CheckIcon class="w-5 h-5" />
        <span>{{ $t('common-actions.save') }}</span>
      </Button>
    </template>
  </Dialog>
</template>