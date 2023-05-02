<script lang="ts" setup>
import { DimensionsString } from '@/types/tankobon-dimensions'
import { convertLocalTimeZoneToUtc, convertUtcToLocalTimeZone } from '@/utils/date'
import { positiveDecimal } from '@/utils/validation'
import { useVuelidate } from '@vuelidate/core'
import { helpers, required, integer, minValue } from '@vuelidate/validators'

export interface BookMetadataFormProps {
  code: string,
  barcode: string | null | undefined,
  number: string,
  title: string,
  synopsis: string,
  notes: string,
  pageCount: string,
  billedAt: string | null | undefined,
  boughtAt: string | null | undefined,
  arrivedAt: string | null | undefined,
  dimensions: DimensionsString,
  mode?: 'creation' | 'update',
}

export type BookMetadataFormEmits = {
  (e: 'update:code', code: string): void,
  (e: 'update:barcode', barcode: string): void,
  (e: 'update:number', number: string): void,
  (e: 'update:title', title: string): void,
  (e: 'update:synopsis', synopsis: string): void,
  (e: 'update:notes', notes: string): void,
  (e: 'update:billedAt', billedAt: string): void,
  (e: 'update:arrivedAt', arrivedAt: string): void,
  (e: 'update:boughtAt', boughtAt: string): void,
  (e: 'update:pageCount', pageCount: string): void,
  (e: 'update:dimensions', dimensions: DimensionsString): void,
  (e: 'validate', isValid: boolean): void,
}

const props = withDefaults(defineProps<BookMetadataFormProps>(), {
  mode: 'creation',
})
const emit = defineEmits<BookMetadataFormEmits>()

const { code, title, pageCount, dimensions } = toRefs(props)

const { t } = useI18n()

const rules = computed(() => {
  const messageRequired = helpers.withMessage(t('validation.required'), required)
  const messageInteger = helpers.withMessage(t('validation.integer'), integer)
  const messageMinValue = helpers.withMessage(({ $params }) => t('validation.min-value', [$params.min]), minValue(0))
  const messageDecimal = helpers.withMessage(t('validation.decimal'), positiveDecimal)

  return {
    code: { messageRequired },
    title: { messageRequired },
    pageCount: { messageInteger, messageMinValue },
    dimensions: {
      widthCm: { messageDecimal },
      heightCm: { messageDecimal },
    }
  }
})

const v$ = useVuelidate(rules, { code, title, pageCount, dimensions })

watch(() => v$.value.$error, (isValid) => emit('validate', isValid))

defineExpose({ v$ })

function handleDateTimeInput(event: KeyboardEvent, field: 'boughtAt' | 'billedAt' | 'arrivedAt') {
  const input = event.target as HTMLInputElement
  console.log(input.value)
  const value = convertLocalTimeZoneToUtc(input.value)

  if (field === 'boughtAt') {
    emit('update:boughtAt', value)
  } else if (field === 'billedAt') {
    emit('update:billedAt', value)
  } else {
    emit('update:arrivedAt', value)
  }
}
</script>

<template>
  <div class="space-y-2">
    <TextInput
      :model-value="title ?? ''"
      id="title"
      required
      :label-text="$t('common-fields.title')"
      :placeholder="$t('common-placeholders.book-title')"
      :invalid="v$.title.$error"
      :errors="v$.title.$errors"
      @blur="v$.title.$touch()"
      @input="$emit('update:title', $event.target.value)"
    />

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-2">
      <TextInput
        :model-value="code ?? ''"
        id="code"
        required
        :label-text="$t('common-fields.code')"
        :placeholder="$t('common-placeholders.book-code')"
        :invalid="v$.code.$error"
        :errors="v$.code.$errors"
        @blur="v$.code.$touch()"
        @input="$emit('update:code', $event.target.value)"
      />

      <TextInput
        :model-value="barcode ?? ''"
        id="barcode"
        required
        inputmode="numeric"
        :label-text="$t('common-fields.barcode')"
        :placeholder="$t('common-placeholders.book-barcode')"
        @input="$emit('update:barcode', $event.target.value)"
      />

      <TextInput
        :model-value="number ?? ''"
        id="number"
        required
        inputmode="decimal"
        :label-text="$t('common-fields.number')"
        :placeholder="$t('common-placeholders.book-number')"
        @input="$emit('update:number', $event.target.value)"
      />
    </div>

    <MarkdownInput
      :model-value="synopsis ?? ''"
      id="synopsis"
      rows="10"
      :label-text="$t('common-fields.synopsis')"
      :placeholder="$t('common-placeholders.book-synopsis')"
      @input="$emit('update:synopsis', $event.target.value)"
    />

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-2">
      <TextInput
        :model-value="boughtAt ? convertUtcToLocalTimeZone(boughtAt) : ''"
        id="bought-at"
        type="datetime-local"
        :label-text="$t('common-fields.bought-at')"
        @input="handleDateTimeInput($event, 'boughtAt')"
      />

      <TextInput
        :model-value="billedAt ? convertUtcToLocalTimeZone(billedAt) : ''"
        id="billed-at"
        type="datetime-local"
        :label-text="$t('common-fields.billed-at')"
        @input="handleDateTimeInput($event, 'billedAt')"
      />

      <TextInput
        :model-value="arrivedAt ? convertUtcToLocalTimeZone(arrivedAt) : ''"
        id="arrived-at"
        type="datetime-local"
        :label-text="$t('common-fields.arrived-at')"
        @input="handleDateTimeInput($event, 'arrivedAt')"
      />
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-2">
      <TextInput
        :model-value="pageCount ?? ''"
        id="page-count"
        required
        inputmode="numeric"
        :input-mask="{
          regex: '\\d+',
          showMaskOnHover: false,
          showMaskOnFocus: false,
        }"
        :placeholder="$t('common-placeholders.book-page-count')"
        :label-text="$t('common-fields.page-count')"
        :invalid="v$.pageCount.$error"
        :errors="v$.pageCount.$errors"
        @blur="v$.pageCount.$touch()"
        @input="$emit('update:pageCount', $event.target.value)"
      />

      <DimensionsInput
        :model-value="dimensions"
        id="dimensions"
        required
        :placeholder-width="$t('common-placeholders.book-width-cm')"
        :placeholder-height="$t('common-placeholders.book-height-cm')"
        :invalid-width="v$.dimensions.widthCm.$error"
        :errors-width="v$.dimensions.widthCm.$errors"
        :invalid-height="v$.dimensions.heightCm.$error"
        :errors-height="v$.dimensions.heightCm.$errors"
        @blur:width="v$.dimensions.widthCm.$touch()"
        @blur:height="v$.dimensions.heightCm.$touch()"
        @update:model-value="$emit('update:dimensions', $event)"
      />
    </div>

    <MarkdownInput
      :model-value="notes ?? ''"
      id="notes"
      rows="5"
      :label-text="$t('common-fields.notes')"
      :placeholder="$t('common-placeholders.book-notes')"
      @input="$emit('update:notes', $event.target.value)"
    />
  </div>
</template>
