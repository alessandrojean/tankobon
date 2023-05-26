<script setup lang="ts">
import useVuelidate from '@vuelidate/core'
import { helpers } from '@vuelidate/validators'
import { TrashIcon } from '@heroicons/vue/20/solid'
import { UserIcon } from '@heroicons/vue/24/outline'
import { maxFileSize } from '@/utils/validation'
import { ACCEPTED_IMAGE_FORMATS } from '@/utils/api'

export interface UserAvatarFormProps {
  currentImageUrl?: string | undefined | null
  avatar: Avatar
  disabled?: boolean
}

export interface Avatar {
  removeExisting: boolean
  file: File | null
}

export interface UserAvatarFormEmits {
  (e: 'update:avatar', avatar: Avatar): void
}

const props = withDefaults(defineProps<UserAvatarFormProps>(), {
  currentImageUrl: undefined,
  disabled: false,
})

const emit = defineEmits<UserAvatarFormEmits>()

const { avatar, currentImageUrl } = toRefs(props)

const { t } = useI18n()

const rules = computed(() => {
  const messageMaxFileSize = helpers.withMessage(
    ({ $params }) => t('validation.max-size', [$params.sizeString]),
    maxFileSize(5 * 1_024 * 1_024, '5MB'),
  )

  return { avatar: { file: { messageMaxFileSize } } }
})

const v$ = useVuelidate(rules, { avatar })

defineExpose({ v$ })

const uploadingBlobUrl = computed(() => {
  if (!avatar.value.file || v$.value.avatar.file.$error) {
    return null
  }

  return URL.createObjectURL(avatar.value.file)
})

function handleUpload(files: FileList) {
  const copy = structuredClone(toRaw(avatar.value))
  copy.file = files[0]

  emit('update:avatar', copy)
  v$.value.avatar.file.$touch()
}

function handleRemove() {
  const copy = structuredClone(toRaw(avatar.value))

  if (uploadingBlobUrl.value) {
    copy.file = null
  } else if (currentImageUrl.value) {
    copy.removeExisting = true
  }

  emit('update:avatar', copy)
}

const previewUrl = computed(() => {
  return avatar.value.removeExisting ? null : currentImageUrl.value
})
</script>

<template>
  <fieldset :disabled="disabled" class="grid grid-cols-1 sm:grid-cols-4 xl:grid-cols-5 gap-4">
    <div class="flex flex-col items-center gap-4">
      <FadeTransition>
        <div
          v-if="!uploadingBlobUrl && !previewUrl"
          :class="[
            'w-2/3 sm:w-full aspect-1 flex items-center justify-center',
            'bg-gray-200 dark:bg-gray-800 rounded-xl shadow-md',
          ]"
        >
          <UserIcon class="w-10 h-10 text-gray-500 dark:text-gray-600" />
        </div>
        <img
          v-else
          :src="uploadingBlobUrl ?? previewUrl ?? undefined"
          class="w-2/3 sm:w-full rounded-xl shadow-md ring-1 ring-black/5 aspect-1 object-cover"
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
        id="avatar"
        class="h-48"
        :accept="ACCEPTED_IMAGE_FORMATS"
        :invalid="v$.avatar.file.$error"
        :errors="v$.avatar.file.$errors"
        @change="handleUpload"
      />
    </div>
  </fieldset>
</template>
