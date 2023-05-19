<script lang="ts" setup>
import { UserIcon } from '@heroicons/vue/20/solid'
import { CheckIcon } from '@heroicons/vue/24/outline'
import { getRelationship } from '@/utils/api'

export interface LibrarySelectorDialogProps {
  isOpen: boolean
}

export interface LibrarySelectorDialogEmits {
  (e: 'close'): void
}

const props = defineProps<LibrarySelectorDialogProps>()
const emit = defineEmits<LibrarySelectorDialogEmits>()

const { t } = useI18n()
const { isOpen } = toRefs(props)
const notificator = useToaster()
const userStore = useUserStore()
const userId = computed(() => userStore.me?.id)
const libraryStore = useLibraryStore()

const { data: libraries } = useUserLibrariesByUserQuery({
  userId: userId as ComputedRef<string>,
  includes: ['owner'],
  includeShared: true,
  enabled: computed(() => userStore.isAuthenticated),
  onError: async (error) => {
    await notificator.failure({
      title: t('libraries.fetch-failure'),
      body: error.message,
    })
  },
  initialData: [],
})

const library = computed(() => libraryStore.library!)
const selected = ref('')

watch([isOpen, libraries, library], () => {
  selected.value = library.value.id
})

function handleSubmit() {
  const selectedLibrary = libraries.value?.find(l => l.id === selected.value)
  libraryStore.setLibrary(selectedLibrary ?? library.value)
  emit('close')
}
</script>

<template>
  <Dialog
    as="form"
    novalidate
    autocomplete="off"
    dialog-class="max-w-lg"
    body-paddingless
    :is-open="isOpen"
    :title="$t('libraries.select')"
    :full-height="false"
    @submit.prevent="handleSubmit"
    @close="$emit('close')"
  >
    <template #default>
      <RadioGroup v-model="selected">
        <RadioGroupLabel class="sr-only">
          {{ $t('entities.libraries') }}
        </RadioGroupLabel>
        <div class="divide-y dark:divide-gray-700 select-none">
          <RadioGroupOption
            v-for="libraryOption in libraries"
            :key="libraryOption.id"
            v-slot="{ checked }"
            :value="libraryOption.id"
            as="template"
          >
            <div class="px-4 md:px-6 py-3 md:py-4 gap-4 md:gap-6 flex items-center cursor-pointer">
              <div class="grow">
                <div class="font-medium">
                  {{ libraryOption.attributes.name }}
                </div>
                <div class="flex items-center mt-0.5">
                  <UserIcon class="w-4 h-4 text-gray-400 dark:text-gray-500 -ml-1" />
                  <span class="ml-1.5 text-sm text-gray-700 dark:text-gray-300">
                    {{ getRelationship(libraryOption, 'OWNER')!.attributes!.name }}
                  </span>
                </div>
              </div>
              <CheckIcon
                v-if="checked"
                class="w-6 h-6 text-primary-600 dark:text-primary-500 shrink-0"
              />
            </div>
          </RadioGroupOption>
        </div>
      </RadioGroup>
    </template>
    <template #footer>
      <Button
        type="submit"
        class="ml-auto"
        kind="primary"
      >
        {{ $t('common-actions.select') }}
      </Button>
    </template>
  </Dialog>
</template>
