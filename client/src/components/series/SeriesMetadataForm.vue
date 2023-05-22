<script lang="ts" setup>
import { useVuelidate } from '@vuelidate/core'
import { helpers, required } from '@vuelidate/validators'
import type { AlternativeName } from '@/types/tankobon-alternative-name'
import type { SeriesType } from '@/types/tankobon-series'
import { BCP47_OPTIONS, getLanguageName, getRegionCode } from '@/utils/language'

export interface SeriesMetadataFormProps {
  name: string
  description: string
  type: SeriesType | null
  alternativeNames: AlternativeName[]
  originalLanguage: string | null
  lastNumber: string | null
  disabled?: boolean
  mode?: 'creation' | 'update'
}

export interface SeriesMetadataFormEmits {
  (e: 'update:name', name: string): void
  (e: 'update:description', description: string): void
  (e: 'update:type', type: string | null): void
  (e: 'update:alternativeNames', alternativeNames: AlternativeName[]): void
  (e: 'update:originalLanguage', originalLanguage: string | null): void
  (e: 'update:lastNumber', lastNumber: string | null): void
  (e: 'validate', isValid: boolean): void
}

const props = withDefaults(defineProps<SeriesMetadataFormProps>(), {
  disabled: false,
  mode: 'creation',
})
const emit = defineEmits<SeriesMetadataFormEmits>()

const { name, description } = toRefs(props)

const { t, locale } = useI18n()

const rules = computed(() => {
  const messageRequired = helpers.withMessage(t('validation.required'), required)

  return {
    name: { messageRequired },
  }
})

const v$ = useVuelidate(rules, { name })

watch(() => v$.value.$error, isValid => emit('validate', isValid))

defineExpose({ v$ })

const types: ('null' | SeriesType)[] = [
  'null',
  'MANGA',
  'MANHWA',
  'MANHUA',
  'COMIC',
  'BOOK',
  'NOVEL',
  'DATABOOK',
  'ARTBOOK',
  'LIGHT_NOVEL',
]

function getSeriesTypeName(type: 'null' | SeriesType | undefined) {
  if (!type || type === 'null') {
    return t('series-types.unknown')
  }

  const typeKey = type.toLowerCase().replace(/_/g, '-')
  return t(`series-types.${typeKey}`)
}

const originalLanguageOptions = ['null', ...BCP47_OPTIONS.filter(l => !l.includes('Latn'))]

function getOriginalLanguageName(language: string | undefined) {
  return getLanguageName({
    language: language === 'null' ? null : language,
    locale: locale.value,
    romanizedLabel: t('original-language.romanized'),
    unknownLabel: t('original-language.unknown'),
  })
}
</script>

<template>
  <fieldset class="space-y-2" :disabled="disabled">
    <TextInput
      id="name"
      :model-value="name"
      required
      :label-text="$t('common-fields.name')"
      :placeholder="$t('common-placeholders.series-name')"
      :invalid="v$.name.$error"
      :errors="v$.name.$errors"
      @blur="v$.name.$touch()"
      @input="$emit('update:name', $event.target.value)"
    />

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-2">
      <SearchableCombobox
        id="type"
        kind="fancy"
        :placeholder="$t('common-placeholders.series-type')"
        :label-text="$t('series.type')"
        :model-value="type === null ? 'null' : type"
        :options="types"
        :option-text="getSeriesTypeName"
        :option-value="t => t"
        :option-value-select="t => t === null ? 'null' : t"
        @update:model-value="$emit('update:type', $event === 'null' ? null : $event)"
        @update:model-value-select="$emit('update:type', $event === 'null' ? null : $event)"
      />

      <SearchableCombobox
        id="original-language"
        kind="fancy"
        :placeholder="$t('common-placeholders.series-original-language')"
        :label-text="$t('original-language.label')"
        :model-value="originalLanguage === null ? 'null' : originalLanguage"
        :options="originalLanguageOptions"
        :option-text="getOriginalLanguageName"
        :option-value="l => l"
        :option-value-select="l => l === null ? 'null' : l"
        @update:model-value="$emit('update:originalLanguage', $event === 'null' ? null : $event)"
        @update:model-value-select="$emit('update:originalLanguage', $event === 'null' ? null : $event)"
      >
        <template #left-icon>
          <div class="absolute left-3 top-9 mt-px">
            <Flag :region="originalLanguage ? getRegionCode(originalLanguage) : null" />
          </div>
        </template>
        <template #option="{ option }: { option: string }">
          <div class="flex items-center gap-3 w-full">
            <Flag
              class="-ml-0.5 shrink-0"
              :region="option === 'null' ? null : getRegionCode(option)"
            />
            <div class="grow">
              {{ getOriginalLanguageName(option) }}
            </div>
          </div>
        </template>
      </SearchableCombobox>

      <TextInput
        id="last-number"
        :model-value="lastNumber ?? ''"
        required
        :label-text="$t('series.last-number')"
        :placeholder="$t('common-placeholders.series-last-number')"
        @input="$emit('update:lastNumber', $event.target.value)"
      />
    </div>

    <MarkdownInput
      id="description"
      :model-value="description"
      rows="8"
      :label-text="$t('common-fields.description')"
      :placeholder="$t('common-placeholders.series-description')"
      @input="$emit('update:description', $event.target.value)"
    />
  </fieldset>
</template>
