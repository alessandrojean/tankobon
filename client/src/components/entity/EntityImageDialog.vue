<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import useVuelidate from '@vuelidate/core'
import { helpers } from '@vuelidate/validators'
import { maxFileSize } from '@/utils/validation'

export interface EntityImageDialogProps {
  currentImageUrl: string | undefined | null
  description: string
  isOpen: boolean
  title: string
}

export interface ImageResult {
  removeExisting: boolean
  file: File | null
}

export interface UserCreateDialogEmits {
  (e: 'close'): void
  (e: 'submit', image: ImageResult): void
}

const props = defineProps<EntityImageDialogProps>()

const emit = defineEmits<UserCreateDialogEmits>()

const ACCEPT_FORMATS = [
  'image/png',
  'image/jpeg',
  'image/bmp',
  'image/gif',
  'image/x-webp',
  'image/webp',
]

const { t } = useI18n()

const { isOpen, currentImageUrl } = toRefs(props)
const formState = reactive({
  file: null as File | null,
})
const removeExisting = ref(false)

const rules = {
  file: {
    maxFileSize: helpers.withMessage(
      ({ $params }) => t('validation.max-size', [$params.sizeString]),
      maxFileSize(5 * 1_024 * 1_024, '5MB'),
    ),
  },
}

const v$ = useVuelidate(rules, formState)

whenever(isOpen, () => v$.value.$reset())

const uploadingBlobUrl = computed(() => {
  if (!formState.file || v$.value.file.$error)
    return null

  return URL.createObjectURL(formState.file)
})

async function handleSubmit() {
  const isValid = await v$.value.$validate()

  if (!isValid)
    return

  emit('submit', {
    removeExisting: removeExisting.value,
    file: formState.file,
  })
  emit('close')
}

function handleRemove() {
  if (uploadingBlobUrl.value)
    formState.file = null
  else if (currentImageUrl.value)
    removeExisting.value = true
}

function handleUpload(files: FileList) {
  formState.file = files[0]
  v$.value.file.$touch()
}

const previewUrl = computed(() => removeExisting.value ? null : currentImageUrl.value)
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-md"
    novalidate
    :is-open="isOpen"
    :title="title"
    :description="description"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <div>
        <div class="flex items-center space-x-3">
          <slot name="preview" :picture-url="uploadingBlobUrl ?? previewUrl">
            <Avatar :picture-url="uploadingBlobUrl ?? previewUrl" />
          </slot>
          <Button
            size="small"
            class="h-fit"
            :disabled="!currentImageUrl && !uploadingBlobUrl"
            @click="handleRemove"
          >
            {{ $t('common-actions.remove') }}
          </Button>
        </div>
        <div class="mt-6">
          <FileInput
            id="image"
            :accept="ACCEPT_FORMATS"
            :invalid="v$.file.$error"
            :errors="v$.file.$errors"
            @change="handleUpload"
          />
        </div>
      </div>
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
