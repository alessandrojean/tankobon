<script lang="ts" setup>
import { UserIcon } from '@heroicons/vue/20/solid'
import { CheckIcon } from '@heroicons/vue/24/outline'
import LibraryForm from '@/components/libraries/LibraryForm.vue'
import { LibraryCreation, LibraryEntity } from '@/types/tankobon-library'
import { getRelationship } from '@/utils/api';

export interface LibrarySelectorDialogProps {
  isOpen: boolean,
}

export type LibrarySelectorDialogEmits = {
  (e: 'close'): void,
}

const props = defineProps<LibrarySelectorDialogProps>()
const emit = defineEmits<LibrarySelectorDialogEmits>()

const { t } = useI18n()
const { isOpen } = toRefs(props)
const notificator = useToaster()
const userStore = useUserStore()
const libraryStore = useLibraryStore()

const { data: libraries } = useUserLibrariesByUserQuery({
  userId: computed(() => userStore.me!.id),
  includes: ['owner'],
  includeShared: true,
  onError: async (error) => {
    await notificator.failure({
      title: t('libraries.fetch-failure'),
      body: error.message,
    })
  },
  initialData: []
})

const library = computed(() => libraryStore.library ?? libraries.value![0])

function setLibrary(library: LibraryEntity) {
  libraryStore.setLibrary(library)
}
</script>

<template>
  <Dialog
    as="div"
    dialog-class="max-w-lg"
    body-paddingless
    :is-open="isOpen"
    :title="$t('libraries.select')"
    :full-height="false"
    @close="$emit('close')"
  >
    <template #default>
      <RadioGroup
        :model-value="library"
        @update:model-value="setLibrary"
      >
        <RadioGroupLabel class="sr-only">
          {{ $t('entities.libraries') }}
        </RadioGroupLabel>
        <div class="divide-y select-none">
          <RadioGroupOption
            v-for="libraryOption in libraries"
            :key="libraryOption.id"
            :value="libraryOption"
            v-slot="{ checked }"
            as="template"
          >
            <div class="px-4 md:px-6 py-3 md:py-4 gap-4 md:gap-6 flex items-center cursor-pointer">
              <div class="grow">
                <div class="font-medium">
                  {{ libraryOption.attributes.name }}
                </div>
                <div class="flex items-center mt-0.5">
                  <UserIcon class="w-4 h-4 text-gray-400 -ml-1" />
                  <span class="ml-1.5 text-sm text-gray-700">
                    {{ getRelationship(libraryOption, 'OWNER')!.attributes!.name }}
                  </span>
                </div>
              </div>
              <CheckIcon
                v-if="checked"
                class="w-6 h-6 text-primary-600 shrink-0"
              />
            </div>
          </RadioGroupOption>
        </div>
      </RadioGroup>
    </template>
    <template #footer>
      <Button
        type="button"
        class="ml-auto"
        kind="primary"
        @click="$emit('close')"
      >
        <CheckIcon class="w-5 h-5" />
        <span>{{ $t('common-actions.select') }}</span>
      </Button>
    </template>
  </Dialog>
</template>
