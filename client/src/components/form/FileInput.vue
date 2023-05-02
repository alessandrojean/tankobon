<script lang="ts" setup>
import mime from 'mime-types'
import type { ErrorObject } from '@vuelidate/core'
import { ExclamationCircleIcon } from '@heroicons/vue/20/solid'

export interface FileInputProps {
  accept?: string[]
  errors?: ErrorObject[]
  invalid?: boolean
  label?: string
}

export interface FileInputEmits {
  (e: 'change', files: FileList): void
}

const props = withDefaults(defineProps<FileInputProps>(), {
  accept: undefined,
  errors: undefined,
  invalid: false,
})

const emit = defineEmits<FileInputEmits>()

const { accept, errors } = toRefs(props)
const isDragging = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

const extensions = computed(() => {
  const ext = accept.value
    ?.map(mimeType => mime.extension(mimeType))
    ?.filter(extension => typeof extension === 'string')
    ?.map(extension => (extension as string).toUpperCase())
    ?.sort()

  return [...new Set(ext)]
})

function handleDrop(e: DragEvent) {
  if (!fileInput.value || !e.dataTransfer?.files)
    return

  fileInput.value.files = e.dataTransfer.files
  isDragging.value = false
  handleChange()
}

function handleChange() {
  emit('change', fileInput.value!.files as FileList)
}

const errorMessage = computed(() => errors.value?.[0]?.$message)
</script>

<script lang="ts">
export default { inheritAttrs: false }
</script>

<template>
  <div
    class="flex flex-col items-center rounded-md border-2 border-dashed px-6 pt-5 pb-6 motion-safe:transition-colors" :class="[
      {
        'border-primary-400 bg-primary-100': isDragging,
        'border-red-400 bg-red-100': invalid && !isDragging,
        'border-gray-300 dark:border-gray-600': !isDragging && !invalid,
      },
    ]"
    @dragover.prevent="isDragging = true"
    @dragleave="isDragging = false"
    @drop.prevent="handleDrop"
  >
    <slot name="icon">
      <svg
        class="mx-auto h-12 w-12" :class="[
          {
            'text-primary-500': isDragging,
            'text-red-500': invalid && !isDragging,
            'text-gray-400 dark:text-gray-500': !isDragging && !invalid,
          },
        ]"
        stroke="currentColor"
        fill="none"
        viewBox="0 0 48 48"
        aria-hidden="true"
      >
        <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
    </slot>
    <div class="flex text-sm text-gray-600">
      <label
        :for="String($attrs.id)"
        class="relative cursor-pointer rounded-md font-medium focus-within:outline-none focus-within:ring-2  focus-within:ring-offset-2" :class="[
          {
            'text-primary-600 focus-within:ring-primary-500 hover:text-primary-500 dark:text-primary-500 dark:hover:text-primary-400': !invalid || isDragging,
            'text-red-600 focus-within:ring-red-500 hover:text-red-500': invalid && !isDragging,
          },
        ]"
      >
        <span>{{ $t('upload.prompt') }}</span>
      </label>
      <input
        v-bind="$attrs"
        ref="fileInput"
        type="file"
        class="sr-only"
        :accept="accept?.join(',')"
        @change="handleChange"
      >
      <p
        class="pl-1" :class="[
          {
            'text-primary-600': isDragging,
            'text-red-600': invalid && !isDragging,
            'dark:text-gray-300': !invalid && !isDragging,
          },
        ]"
      >
        {{ $t('upload.drag-and-drop') }}
      </p>
    </div>
    <p
      class="text-xs" :class="[
        {
          'text-primary-600': isDragging,
          'text-red-600': invalid && !isDragging,
          'text-gray-500 dark:text-gray-400': !invalid && !isDragging,
        },
      ]"
    >
      {{ $t('upload.formats', [extensions.join(', '), '5MB']) }}
    </p>

    <p
      v-if="invalid && errorMessage"
      class="text-red-700 font-medium mt-4 text-sm bg-white px-2 py-1 rounded flex items-center border border-red-200"
    >
      <ExclamationCircleIcon class="w-5 h-5 -ml-1 mr-2" />
      <span>{{ errorMessage }}</span>
    </p>
  </div>
</template>
