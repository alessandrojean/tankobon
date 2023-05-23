<script lang="ts" setup>
import { useVuelidate } from '@vuelidate/core'
import { helpers, maxValue, minValue, or, required } from '@vuelidate/validators'
import { countries as regions } from 'countries-list'
import { addDays } from 'date-fns'
import { BCP47_OPTIONS } from '@/utils/language'

export interface PersonMetadataFormProps {
  name: string
  description: string
  nationality: string | null
  bornAt: string | null
  diedAt: string | null
  nativeName: string
  nativeNameLanguage: string | null
  mode?: 'creation' | 'update'
}

export interface PersonMetadataFormEmits {
  (e: 'update:name', name: string): void
  (e: 'update:description', description: string): void
  (e: 'update:nationality', nationality: string | null): void
  (e: 'update:bornAt', bornAt: string | null): void
  (e: 'update:diedAt', diedAt: string | null): void
  (e: 'update:nativeName', nativeName: string): void
  (e: 'update:nativeNameLanguage', nativeNameLanuage: string | null): void
  (e: 'validate', isValid: boolean): void
}

const props = withDefaults(defineProps<PersonMetadataFormProps>(), {
  mode: 'creation',
})
const emit = defineEmits<PersonMetadataFormEmits>()

const { name, description, bornAt, diedAt } = toRefs(props)

const { t, locale } = useI18n()

const bornAtDate = computed(() => bornAt.value ? new Date(bornAt.value) : 0)
const diedAtDate = computed(() => diedAt.value ? new Date(diedAt.value) : 0)

const rules = computed(() => {
  const messageRequired = helpers.withMessage(t('validation.required'), required)

  const messageMaxToday = helpers.withMessage(
    t('validation.max-value', [t('date.today')]),
    // @ts-expect-error The library forgot to add the Date in the types.
    maxValue(new Date()),
  )

  const validDiedAt = or(
    (value: Date | number) => typeof value === 'number' && value === 0,
    () => bornAt.value === null,
    // @ts-expect-error The library forgot to add the Date in the types.
    minValue(computed(() => addDays(bornAtDate.value, 1))),
  )

  const validBornAt = or(
    (value: Date | number) => typeof value === 'number' && value === 0,
    () => diedAt.value === null,
    // @ts-expect-error The library forgot to add the Date in the types.
    maxValue(computed(() => addDays(diedAtDate.value, -1))),
  )

  const messageBorn = helpers.withMessage(t('validation.durational-person-born'), validBornAt)
  const messageDied = helpers.withMessage(t('validation.durational-person-died'), validDiedAt)

  return {
    name: { messageRequired },
    bornAt: { messageBorn, messageMaxToday },
    diedAt: { messageDied, messageMaxToday },
  }
})

const v$ = useVuelidate(rules, {
  name,
  bornAt: bornAtDate,
  diedAt: diedAtDate,
})

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

function handleDateTimeInput(event: KeyboardEvent, field: 'bornAt' | 'diedAt') {
  const input = event.target as HTMLInputElement
  const value = input.value.length > 0 ? input.value : null

  if (field === 'bornAt') {
    emit('update:bornAt', value)
  } else {
    emit('update:diedAt', value)
  }
}
</script>

<template>
  <div class="space-y-2">
    <TextInput
      id="name"
      :model-value="name"
      required
      :label-text="$t('common-fields.name')"
      :placeholder="$t('common-placeholders.person-name')"
      :invalid="v$.name.$error"
      :errors="v$.name.$errors"
      @blur="v$.name.$touch()"
      @input="$emit('update:name', $event.target.value)"
    />

    <fieldset class="grid grid-cols-1 lg:grid-cols-4 gap-2">
      <div class="lg:col-span-3">
        <AlternativeNameInput
          kind="fancy"
          :label-name="$t('common-fields.native-name')"
          :label-language="$t('common-fields.native-name-language')"
          :disabled-options="[]"
          :name="nativeName"
          :language="nativeNameLanguage ?? 'null'"
          :languages="['null', ...BCP47_OPTIONS]"
          :placeholder="$t('common-placeholders.person-native-name')"
          @update:name="$emit('update:nativeName', $event)"
          @update:language="$emit('update:nativeNameLanguage', $event === 'null' ? null : $event)"
        />
        <!-- <TextInput
          id="native-name"
          :model-value="nativeName"
          required
          :label-text="$t('common-fields.native-name')"
          :placeholder="$t('common-placeholders.person-native-name')"
          @input="$emit('update:nativeName', $event.target.value)"
        /> -->
      </div>

      <SearchableCombobox
        id="location"
        kind="fancy"
        :placeholder="$t('common-placeholders.person-nationality')"
        :label-text="$t('common-fields.nationality')"
        :model-value="nationality === null ? 'null' : nationality"
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
        @update:model-value="$emit('update:nationality', $event === 'null' ? null : $event)"
        @update:model-value-select="$emit('update:nationality', $event === 'null' ? null : $event)"
      >
        <template #left-icon>
          <div class="absolute left-3 top-9 mt-px">
            <Flag :region="nationality" />
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
      :placeholder="$t('common-placeholders.person-description')"
      @input="$emit('update:description', $event.target.value)"
    />

    <div class="grid grid-cols-1 lg:grid-cols-3 xl:grid-cols-8 gap-2">
      <div class="xl:col-span-2">
        <TextInput
          id="born-at"
          :model-value="bornAt ?? ''"
          type="date"
          :label-text="$t('common-fields.born-at')"
          :invalid="v$.bornAt.$error"
          :errors="v$.bornAt.$errors"
          @blur="v$.bornAt.$touch()"
          @input="handleDateTimeInput($event, 'bornAt')"
        />
      </div>
      <div class="xl:col-span-2">
        <TextInput
          id="diedAt-at"
          :model-value="diedAt ?? ''"
          type="date"
          :label-text="$t('common-fields.died-at')"
          :invalid="v$.diedAt.$error"
          :errors="v$.diedAt.$errors"
          @blur="v$.diedAt.$touch()"
          @input="handleDateTimeInput($event, 'diedAt')"
        />
      </div>
    </div>
  </div>
</template>
