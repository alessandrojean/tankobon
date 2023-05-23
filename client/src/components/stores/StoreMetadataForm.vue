<script lang="ts" setup>
import { useVuelidate } from '@vuelidate/core'
import { helpers, required } from '@vuelidate/validators'
import { countries as regions } from 'countries-list'
import type { StoreType } from '@/types/tankobon-store'

export interface StoreMetadataFormProps {
  name: string
  description: string
  location: string | null
  legalName: string
  type: StoreType | null
  mode?: 'creation' | 'update'
}

export interface StoreMetadataFormEmits {
  (e: 'update:name', name: string): void
  (e: 'update:description', description: string): void
  (e: 'update:location', location: string | null): void
  (e: 'update:legalName', legalName: string): void
  (e: 'update:type', type: StoreType | null): void
  (e: 'validate', isValid: boolean): void
}

const props = withDefaults(defineProps<StoreMetadataFormProps>(), {
  mode: 'creation',
})
const emit = defineEmits<StoreMetadataFormEmits>()

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

const regionNames = computed(() => new Intl.DisplayNames(locale.value, {
  style: 'long',
  type: 'region',
}))

interface RegionOption {
  code: string
  name: string
}

const regionsOptions = computed(() => {
  const options = ['null', ...Object.keys(regions)]
    .map(code => ({
      code,
      name: code === 'null'
        ? t('location.unknown')
        : (regionNames.value.of(code) ?? t('location.unknown')),
    }))

  return [
    options[0],
    ...options.slice(1).sort((a, b) => a.name.localeCompare(b.name, locale.value)),
  ] as RegionOption[]
})

const types = computed(() => {
  const options = [
    { type: 'null', text: t('stores-types.unknown') },
    { type: 'BOOKSTORE', text: t('stores-types.bookstore') },
    { type: 'NEWSSTAND', text: t('stores-types.newsstand') },
    { type: 'COMIC_SHOP', text: t('stores-types.comic-shop') },
    { type: 'RETAIL_CHAIN', text: t('stores-types.retail-chain') },
  ]

  return [
    options[0],
    ...options.slice(1).sort((a, b) => a.text.localeCompare(b.type, locale.value)),
  ]
})
</script>

<template>
  <div class="space-y-2">
    <TextInput
      id="name"
      :model-value="name"
      required
      :label-text="$t('common-fields.name')"
      :placeholder="$t('common-placeholders.store-name')"
      :invalid="v$.name.$error"
      :errors="v$.name.$errors"
      @blur="v$.name.$touch()"
      @input="$emit('update:name', $event.target.value)"
    />

    <fieldset class="grid grid-cols-1 lg:grid-cols-4 gap-2">
      <div class="lg:col-span-2">
        <TextInput
          id="legal-name"
          :model-value="legalName"
          required
          :label-text="$t('common-fields.legal-name')"
          :placeholder="$t('common-placeholders.store-legal-name')"
          @input="$emit('update:legalName', $event.target.value)"
        />
      </div>

      <SearchableCombobox
        id="type"
        kind="fancy"
        :placeholder="$t('common-placeholders.store-type')"
        :label-text="$t('stores.type')"
        :model-value="type === null ? 'null' : type"
        :options="types"
        :option-text="(type) => type?.text ?? $t('stores-types.unknown')"
        :option-value="t => t.type"
        :option-value-select="t => t.type"
        @update:model-value="$emit('update:type', $event === 'null' ? null : $event)"
        @update:model-value-select="$emit('update:type', $event === 'null' ? null : $event)"
      />

      <SearchableCombobox
        id="location"
        kind="fancy"
        :placeholder="$t('common-placeholders.store-location')"
        :label-text="$t('common-fields.location')"
        :model-value="location === null ? 'null' : location"
        :options="regionsOptions"
        :option-text="l => l?.name ?? ''"
        :option-value="l => l.code"
        :option-value-select="l => l.code"
        :filter="(query, location) => {
          if (query.length === 2) {
            return query.toLowerCase() === location.code.toLowerCase()
          }

          return query.toLowerCase() === location.code.toLowerCase()
            || location.name.toLowerCase().includes(query.toLowerCase())
        }"
        @update:model-value="$emit('update:location', $event === 'null' ? null : $event)"
        @update:model-value-select="$emit('update:location', $event === 'null' ? null : $event)"
      >
        <template #left-icon>
          <div class="absolute left-3 top-9 mt-px">
            <Flag :region="location" />
          </div>
        </template>
        <template #option="{ option }: { option: RegionOption }">
          <div class="flex items-center gap-3 w-full">
            <Flag
              class="-ml-0.5 shrink-0"
              :region="option.code === 'null' ? null : option.code"
            />
            <div class="grow">
              {{ option.name }}
            </div>
          </div>
        </template>
      </SearchableCombobox>
    </fieldset>

    <MarkdownInput
      id="description"
      :model-value="description"
      rows="8"
      :label-text="$t('common-fields.description')"
      :placeholder="$t('common-placeholders.store-description')"
      @input="$emit('update:description', $event.target.value)"
    />
  </div>
</template>
