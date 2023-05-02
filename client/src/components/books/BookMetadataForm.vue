<script lang="ts" setup>
import { useVuelidate } from '@vuelidate/core'
import { helpers, integer, minValue, required } from '@vuelidate/validators'
import type { DimensionsString } from '@/types/tankobon-dimensions'
import { positiveDecimal } from '@/utils/validation'

export interface BookMetadataFormProps {
  code: string
  barcode: string | null | undefined
  number: string
  title: string
  subtitle: string
  synopsis: string
  pageCount: string
  dimensions: DimensionsString
  mode?: 'creation' | 'update'
}

export interface BookMetadataFormEmits {
  (e: 'update:code', code: string): void
  (e: 'update:barcode', barcode: string): void
  (e: 'update:number', number: string): void
  (e: 'update:title', title: string): void
  (e: 'update:subtitle', subtitle: string): void
  (e: 'update:synopsis', synopsis: string): void
  (e: 'update:pageCount', pageCount: string): void
  (e: 'update:dimensions', dimensions: DimensionsString): void
  (e: 'validate', isValid: boolean): void
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
    },
  }
})

const v$ = useVuelidate(rules, { code, title, pageCount, dimensions })

watch(() => v$.value.$error, isValid => emit('validate', isValid))

defineExpose({ v$ })
</script>

<template>
  <div class="space-y-6">
    <div class="space-y-2">
      <TextInput
        id="title"
        :model-value="title ?? ''"
        required
        :label-text="$t('common-fields.title')"
        :placeholder="$t('common-placeholders.book-title')"
        :invalid="v$.title.$error"
        :errors="v$.title.$errors"
        @blur="v$.title.$touch()"
        @input="$emit('update:title', $event.target.value)"
      />
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-2">
        <div class="lg:col-span-3">
          <TextInput
            id="subtitle"
            :model-value="subtitle ?? ''"
            :label-text="$t('common-fields.subtitle')"
            :placeholder="$t('common-placeholders.book-subtitle')"
            @input="$emit('update:subtitle', $event.target.value)"
          />
        </div>
        <TextInput
          id="number"
          :model-value="number ?? ''"
          required
          inputmode="decimal"
          :label-text="$t('common-fields.number')"
          :placeholder="$t('common-placeholders.book-number')"
          @input="$emit('update:number', $event.target.value)"
        />
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-2">
      <TextInput
        id="code"
        :model-value="code ?? ''"
        required
        :label-text="$t('common-fields.code')"
        :placeholder="$t('common-placeholders.book-code')"
        :invalid="v$.code.$error"
        :errors="v$.code.$errors"
        @blur="v$.code.$touch()"
        @input="$emit('update:code', $event.target.value)"
      />

      <TextInput
        id="barcode"
        :model-value="barcode ?? ''"
        required
        inputmode="numeric"
        :label-text="$t('common-fields.barcode')"
        :placeholder="$t('common-placeholders.book-barcode')"
        @input="$emit('update:barcode', $event.target.value)"
      />
    </div>

    <MarkdownInput
      id="synopsis"
      :model-value="synopsis ?? ''"
      rows="10"
      :label-text="$t('common-fields.synopsis')"
      :placeholder="$t('common-placeholders.book-synopsis')"
      @input="$emit('update:synopsis', $event.target.value)"
    />

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-2">
      <TextInput
        id="page-count"
        :model-value="pageCount ?? ''"
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
        id="dimensions"
        :model-value="dimensions"
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
  </div>
</template>
