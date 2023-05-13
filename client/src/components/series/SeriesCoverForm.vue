<script setup lang="ts">
import useVuelidate from '@vuelidate/core'
import { helpers } from '@vuelidate/validators'
import { TrashIcon } from '@heroicons/vue/20/solid'
import { Square2StackIcon } from '@heroicons/vue/24/outline'
import { maxFileSize } from '@/utils/validation'
import { ACCEPTED_IMAGE_FORMATS } from '@/utils/api'

export interface SeriesCoverFormProps {
  currentImageUrl?: string | undefined | null
  cover: Cover
  disabled?: boolean
}

export interface Cover {
  removeExisting: boolean
  file: File | null
}

export interface SeriesCoverFormEmits {
  (e: 'update:cover', cover: Cover): void
}

const props = withDefaults(defineProps<SeriesCoverFormProps>(), {
  currentImageUrl: undefined,
  disabled: false,
})

const emit = defineEmits<SeriesCoverFormEmits>()

const { cover, currentImageUrl } = toRefs(props)

const { t } = useI18n()

const rules = computed(() => {
  const messageMaxFileSize = helpers.withMessage(
    ({ $params }) => t('validation.max-size', [$params.sizeString]),
    maxFileSize(5 * 1_024 * 1_024, '5MB'),
  )

  return { cover: { file: { messageMaxFileSize } } }
})

const v$ = useVuelidate(rules, { cover })

defineExpose({ v$ })

const uploadingBlobUrl = computed(() => {
  if (!cover.value.file || v$.value.cover.file.$error) {
    return null
  }

  return URL.createObjectURL(cover.value.file)
})

function handleUpload(files: FileList) {
  const copy = structuredClone(toRaw(cover.value))
  copy.file = files[0]

  emit('update:cover', copy)
  v$.value.cover.file.$touch()
}

function handleRemove() {
  const copy = structuredClone(toRaw(cover.value))

  if (uploadingBlobUrl.value) {
    copy.file = null
  } else if (currentImageUrl.value) {
    copy.removeExisting = true
  }

  emit('update:cover', copy)
}

const previewUrl = computed(() => {
  return cover.value.removeExisting ? null : currentImageUrl.value
})
</script>

<template>
  <fieldset :disabled="disabled" class="grid grid-cols-1 sm:grid-cols-4 xl:grid-cols-5 gap-4">
    <div class="flex flex-col items-center gap-4">
      <FadeTransition>
        <div
          v-if="!uploadingBlobUrl && !previewUrl"
          :class="[
            'w-2/3 sm:w-full aspect-[2/3] flex items-center justify-center',
            'bg-gray-200 dark:bg-gray-800 rounded-xl shadow-md',
          ]"
        >
          <Square2StackIcon class="w-10 h-10 text-gray-500 dark:text-gray-600" />
        </div>
        <img
          v-else
          :src="uploadingBlobUrl ?? previewUrl ?? undefined"
          class="w-2/3 sm:w-full rounded-xl shadow-md ring-1 ring-black/5"
        >
      </FadeTransition>
      <Button
        class="w-fit sm:w-full"
        :disabled="!previewUrl && !uploadingBlobUrl"
        @click="handleRemove"
      >
        <TrashIcon class="w-5 h-5" />
        <span>{{ $t('common-actions.remove') }}</span>
      </Button>
    </div>
    <div class="sm:col-span-3 xl:col-span-4">
      <FileInput
        id="cover-art"
        class="h-48"
        :accept="ACCEPTED_IMAGE_FORMATS"
        :invalid="v$.cover.file.$error"
        :errors="v$.cover.file.$errors"
        @change="handleUpload"
      />
    </div>
  </fieldset>
</template>
