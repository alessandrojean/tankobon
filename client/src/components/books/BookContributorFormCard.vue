<script setup lang="ts">
import { XMarkIcon } from '@heroicons/vue/20/solid'
import { helpers, required } from '@vuelidate/validators'
import useVuelidate from '@vuelidate/core'
import { getFullImageUrl } from '@/modules/api'
import type { ContributorRoleEntity } from '@/types/tankobon-contributor-role'
import type { ImageDetailsAttributes } from '@/types/tankobon-image-details'
import type { PersonEntity } from '@/types/tankobon-person'
import { getRelationship } from '@/utils/api'

export interface BookContributorFormCardProps {
  draggable: boolean
  index: number
  invalid?: boolean
  people: PersonEntity[]
  person: PersonEntity
  personPicture: ImageDetailsAttributes | undefined
  role: ContributorRoleEntity
  roles: ContributorRoleEntity[]
}

const props = withDefaults(defineProps<BookContributorFormCardProps>(), {
  invalid: false,
})

defineEmits<{
  (e: 'update:role', roleId: string): void
  (e: 'update:person', personId: string): void
  (e: 'click:remove'): void
}>()

const { role, person } = toRefs(props)
const { t } = useI18n()

const rules = computed(() => {
  const messageRequired = helpers.withMessage(t('validation.required'), required)

  return {
    person: { messageRequired },
    role: { messageRequired },
  }
})

const v$ = useVuelidate(rules, { role, person })

defineExpose({ v$ })

function getPersonPicture(person: PersonEntity) {
  return getRelationship(person, 'PERSON_PICTURE')
}
</script>

<template>
  <div
    :class="[
      'py-4 md:py-3 px-4 rounded-xl',
      'flex flex-col md:flex-row md:items-start gap-4',
      'motion-safe:transition',
      'bg-gray-100 dark:bg-gray-900',
      {
        'ring-2 ring-red-600 dark:ring-red-500/60': invalid,
      },
    ]"
  >
    <div class="flex items-start gap-4 grow">
      <span
        :class="[
          'grabber shrink-0 text-gray-500 dark:text-gray-300 mt-2.5',
          {
            'cursor-grab': draggable,
            'opacity-50': !draggable,
          },
        ]"
      >
        <svg aria-hidden="true" viewBox="0 0 16 16" version="1.1" class="w-5 h-5" fill="currentColor">
          <path d="M10 13a1 1 0 1 1 0-2 1 1 0 0 1 0 2Zm0-4a1 1 0 1 1 0-2 1 1 0 0 1 0 2Zm-4 4a1 1 0 1 1 0-2 1 1 0 0 1 0 2Zm5-9a1 1 0 1 1-2 0 1 1 0 0 1 2 0ZM7 8a1 1 0 1 1-2 0 1 1 0 0 1 2 0ZM6 5a1 1 0 1 1 0-2 1 1 0 0 1 0 2Z" />
        </svg>
      </span>
      <SearchableCombobox
        :id="`person-input-${index}`"
        class="grow"
        :placeholder="$t('common-placeholders.book-contributor-person')"
        :label-text="$t('common-fields.person')"
        :model-value="person"
        :options="people ?? []"
        :option-text="(p: PersonEntity) => p?.attributes?.name"
        :option-value="(p: PersonEntity) => p"
        :option-value-select="(p: PersonEntity) => p?.id"
        :invalid="v$.person.$error"
        :errors="v$.person.$errors"
        @blur="v$.person.$touch()"
        @update:model-value="$emit('update:person', $event.id)"
        @update:model-value-select="$emit('update:person', $event)"
      >
        <template #left-icon>
          <div class="absolute left-2 top-1/2 -translate-y-1/2">
            <Avatar
              size="extra-mini"
              :picture-url="
                getFullImageUrl({
                  collection: 'people',
                  fileName: personPicture?.versions['128'],
                  timeHex: personPicture?.timeHex,
                })
              "
            />
          </div>
        </template>
        <template #option="{ option }">
          <div class="flex items-center gap-3 w-full">
            <Avatar
              class="-ml-0.5 shrink-0"
              size="extra-mini"
              :picture-url="
                getFullImageUrl({
                  collection: 'people',
                  fileName: getPersonPicture(option)?.attributes?.versions['128'],
                  timeHex: getPersonPicture(option)?.attributes?.timeHex,
                })
              "
            />
            <div class="grow">
              {{ (option as PersonEntity).attributes!.name }}
            </div>
          </div>
        </template>
      </SearchableCombobox>
      <Button
        class="shrink-0 w-10 h-10 -mx-2 md:hidden"
        kind="ghost-alt"
        size="small"
        :title="$t('common-actions.remove')"
        @click="$emit('click:remove')"
      >
        <span class="sr-only">{{ $t('common-actions.remove') }}</span>
        <XMarkIcon class="w-5 h-5" />
      </Button>
    </div>
    <div class="flex items-start gap-4 grow">
      <span class="shrink-0 select-none w-fit mt-2">
        {{ $t('people.role-as') }}
      </span>
      <SearchableCombobox
        class="grow"
        :placeholder="$t('common-placeholders.book-contributor-role')"
        :label-text="$t('common-fields.role')"
        :model-value="role"
        :options="roles ?? []"
        :option-text="(r: ContributorRoleEntity) => r?.attributes?.name"
        :option-value="(r: ContributorRoleEntity) => r"
        :option-value-select="(r: ContributorRoleEntity) => r?.id"
        :invalid="v$.role.$error"
        :errors="v$.role.$errors"
        @blur="v$.role.$touch()"
        @update:model-value="$emit('update:role', $event.id)"
        @update:model-value-select="$emit('update:role', $event)"
      />
    </div>
    <Button
      class="shrink-0 w-10 h-10 -mr-2 hidden md:flex mt-px"
      kind="ghost-alt"
      size="small"
      :title="$t('common-actions.remove')"
      @click="$emit('click:remove')"
    >
      <span class="sr-only">{{ $t('common-actions.remove') }}</span>
      <XMarkIcon class="w-5 h-5" />
    </Button>
  </div>
</template>
