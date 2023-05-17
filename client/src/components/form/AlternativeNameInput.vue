<script lang="ts" setup>
import type { ErrorObject } from '@vuelidate/core'
import Button from './Button.vue'
import { getFlagScriptCode, getLanguageName, getRegionCode } from '@/utils/language'

export interface TextInputProps {
  errorsLanguage?: ErrorObject[]
  errorsName?: ErrorObject[]
  index: number
  invalidLanguage?: boolean
  invalidName?: boolean
  language: string
  languages: string[]
  name: string
  placeholder?: string
}

const props = withDefaults(defineProps<TextInputProps>(), {
  errorsLanguage: undefined,
  errorsName: undefined,
  invalidLanguage: false,
  invalidName: false,
})

const emit = defineEmits<{
  (e: 'update:name', name: string): void
  (e: 'update:language', language: string): void
  (e: 'blur:name'): void
  (e: 'blur:language'): void
}>()

const { t, locale } = useI18n()
const { errorsLanguage, errorsName } = toRefs(props)

function getOriginalLanguageName(language: string) {
  return getLanguageName({
    language: language === 'null' ? null : language,
    locale: locale.value,
    romanizedLabel: t('original-language.romanized'),
    unknownLabel: t('original-language.unknown'),
  })
}

const errorMessage = computed(() => {
  const errorLanguage = unref(errorsLanguage.value?.[0]?.$message)
  const errorName = unref(errorsName.value?.[0]?.$message)

  if (errorLanguage === errorName) {
    return errorName
  } else if (errorLanguage && errorLanguage.length > 0) {
    return errorLanguage
  } else if (errorName && errorName.length > 0) {
    return errorName
  } else {
    return null
  }
})

function handleNameChange(newName: string) {
  emit('update:name', newName)
  emit('blur:name')
  emit('blur:language')
}

function handleLanguageChange(newLanguage: string) {
  emit('update:language', newLanguage)
  emit('blur:language')
}

function blurBothFields() {
  emit('blur:name')
  emit('blur:language')
}
</script>

<template>
  <fieldset class="relative disabled:opacity-50">
    <label
      :for="`name-input-${index}`"
      class="sr-only"
    >
      {{ $t('alternative-names.label-name', [index + 1]) }}
    </label>
    <input
      :id="`name-input-${index}`"
      :class="[
        'peer w-full bg-white dark:bg-gray-950 shadow-sm rounded-md',
        'dark:text-gray-200 focus:ring focus:ring-opacity-50',
        'motion-safe:transition-shadow placeholder:text-gray-500',
        'border focus:outline-none pl-[3.6rem] h-10',
        invalidName || invalidLanguage
          ? 'border-red-500 dark:border-red-500/95 focus:border-red-500 dark:focus:border-red-500/95 focus:ring-red-200 dark:focus:ring-red-200/30'
          : 'border-gray-300 dark:border-gray-800 focus:border-primary-500 dark:focus:border-primary-400 focus:ring-primary-200 dark:focus:ring-primary-200/30',
      ]"
      :placeholder="placeholder"
      :value="name"
      :lang="language !== 'null' ? language : undefined"
      @blur="blurBothFields"
      @input="handleNameChange(($event.target! as HTMLInputElement).value)"
    >
    <div
      :class="[
        'absolute h-7 flex items-center justify-center',
        'motion-safe:transition-colors left-1.5 top-0',
        'border-r border-gray-300 dark:border-gray-800 my-1.5 pr-1.5',
      ]"
    >
      <BasicListbox
        :check-icon="false"
        :label-text="$t('alternative-names.label-language', [index + 1])"
        :model-value="language === '' ? 'null' : language"
        :options="languages"
        :option-text="getOriginalLanguageName"
        :option-value="l => l"
        :disabled-options="[0]"
        @update:model-value="handleLanguageChange"
      >
        <template #listbox-button>
          <ListboxButton
            :as="Button"
            class="w-9 h-9 !p-0"
            kind="ghost-alt"
            size="mini"
            :title="$t('alternative-names.label-language', [index + 1])"
          >
            <span class="sr-only">{{ $t('alternative-names.label-language', [index + 1]) }}</span>
            <Flag
              :region="language !== 'null' ? getRegionCode(language) : null"
              :script="language !== 'null' ? getFlagScriptCode(language) : undefined"
            />
          </ListboxButton>
        </template>
        <template #option="{ option }: { option: string }">
          <div class="flex items-center gap-3 w-full">
            <Flag
              class="-ml-0.5 shrink-0"
              :region="option === 'null' ? null : getRegionCode(option)"
              :script="option !== 'null' ? getFlagScriptCode(option) : undefined"
            />
            <div class="grow">
              {{ getOriginalLanguageName(option) }}
            </div>
          </div>
        </template>
      </BasicListbox>
    </div>

    <p
      v-if="(invalidLanguage || invalidName) && errorMessage"
      class="text-red-700 dark:text-red-500/95 text-sm font-medium ml-2 mt-1"
    >
      {{ errorMessage }}
    </p>
  </fieldset>
</template>
