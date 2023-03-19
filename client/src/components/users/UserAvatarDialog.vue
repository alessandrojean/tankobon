<script lang="ts" setup>
import type { UserEntity, UserUpdate } from '@/types/tankobon-user'
import { CheckIcon } from '@heroicons/vue/20/solid'
import { getFullImageUrl } from '@/modules/api';
import { maxFileSize } from '@/utils/validation';
import useVuelidate from '@vuelidate/core';
import { helpers } from '@vuelidate/validators';
import { getRelationship } from '@/utils/api';

export interface UserCreateDialogProps {
  isOpen: boolean,
  userEntity: UserEntity,
}

export interface AvatarResult {
  removeExisting: boolean,
  file: File | null,
}

export type UserCreateDialogEmits = {
  (e: 'close'): void,
  (e: 'submit', user: AvatarResult): void,
}

const ACCEPT_FORMATS = [
  'image/png',
  'image/jpeg',
  'image/bmp',
  'image/gif',
  'image/x-webp',
  'image/webp',
]

const props = defineProps<UserCreateDialogProps>()
const emit = defineEmits<UserCreateDialogEmits>()
const { t } = useI18n()

const { isOpen, userEntity } = toRefs(props)
const formState = reactive({
  file: null as File | null,
})
const removeExisting = ref(false)

const rules = {
  file: {
    maxFileSize: helpers.withMessage(
      ({ $params }) => t('validation.maxSize', [$params.sizeString]),
      maxFileSize(5 * 1_024 * 1_024, '5MB')
    )
  }
}

const v$ = useVuelidate(rules, formState)

whenever(isOpen, () => v$.value.$reset())

const currentAvatarUrl = computed(() => {
  if (removeExisting.value) {
    return null
  }

  const avatar = getRelationship(userEntity.value, 'AVATAR')

  return getFullImageUrl({
    collection: 'avatars',
    fileName: avatar?.attributes?.versions['128'],
    timeHex: avatar?.attributes?.timeHex,
  })
})

const uploadingBlobUrl = computed(() => {
  if (!formState.file || v$.value.file.$error) {
    return null
  }

  return URL.createObjectURL(formState.file)
})

async function handleSubmit() {
  const isValid = await v$.value.$validate()

  if (!isValid) {
    return
  }

  emit('submit', {
    removeExisting: removeExisting.value,
    file: formState.file,
  })
  emit('close')
}

function handleRemove() {
  if (uploadingBlobUrl.value) {
    formState.file = null
  } else if (currentAvatarUrl.value) {
    removeExisting.value = true
  }
}

function handleUpload(files: FileList) {
  formState.file = files[0]
  v$.value.file.$touch()
}
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-md"
    novalidate
    :is-open="isOpen"
    :title="$t('users.edit-avatar-header')"
    :description="$t('users.edit-avatar-description')"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <div>
        <div class="flex items-center space-x-3">
          <Avatar :picture-url="uploadingBlobUrl ?? currentAvatarUrl" />
          <Button
            size="small"
            class="h-fit"
            :disabled="!currentAvatarUrl && !uploadingBlobUrl"
            @click="handleRemove"
          >
            {{ $t('common-actions.remove') }}
          </Button>
        </div>
        <div class="mt-6">
          <FileInput
            id="avatar"
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