<script lang="ts" setup>
import { useVuelidate } from '@vuelidate/core'
import { helpers, required } from '@vuelidate/validators'
import MonetaryAmountInput from '../form/MonetaryAmountInput.vue'
import type { MonetaryAmountString } from '@/types/tankobon-monetary'
import { convertLocalTimeZoneToUtc, convertUtcToLocalTimeZone } from '@/utils/date'
import { positiveDecimal } from '@/utils/validation'

export interface BookMetadataFormProps {
  notes: string
  billedAt: string | null | undefined
  boughtAt: string | null | undefined
  arrivedAt: string | null | undefined
  labelPrice: MonetaryAmountString
  paidPrice: MonetaryAmountString
  mode?: 'creation' | 'update'
}

export interface BookMetadataFormEmits {
  (e: 'update:notes', notes: string): void
  (e: 'update:billedAt', billedAt: string): void
  (e: 'update:arrivedAt', arrivedAt: string): void
  (e: 'update:boughtAt', boughtAt: string): void
  (e: 'update:labelPrice', labelPrice: MonetaryAmountString): void
  (e: 'update:paidPrice', paidPrice: MonetaryAmountString): void
  (e: 'validate', isValid: boolean): void
}

const props = withDefaults(defineProps<BookMetadataFormProps>(), {
  mode: 'creation',
})
const emit = defineEmits<BookMetadataFormEmits>()

const { paidPrice, labelPrice } = toRefs(props)

const { t } = useI18n()

const rules = computed(() => {
  const messageRequired = helpers.withMessage(t('validation.required'), required)
  const messageDecimal = helpers.withMessage(t('validation.decimal'), positiveDecimal)

  return {
    labelPrice: {
      amount: { messageRequired, messageDecimal },
      currency: { messageRequired },
    },
    paidPrice: {
      amount: { messageRequired, messageDecimal },
      currency: { messageRequired },
    },
  }
})

const v$ = useVuelidate(rules, { paidPrice, labelPrice })

watch(() => v$.value.$error, isValid => emit('validate', isValid))

defineExpose({ v$ })

function handleDateTimeInput(event: KeyboardEvent, field: 'boughtAt' | 'billedAt' | 'arrivedAt') {
  const input = event.target as HTMLInputElement
  const value = convertLocalTimeZoneToUtc(input.value)

  if (field === 'boughtAt')
    emit('update:boughtAt', value)
  else if (field === 'billedAt')
    emit('update:billedAt', value)
  else
    emit('update:arrivedAt', value)
}
</script>

<template>
  <div class="space-y-6">
    <div class="grid grid-cols-1 lg:grid-cols-4 gap-2">
      <MonetaryAmountInput
        id="label-price"
        :model-value="labelPrice"
        :label-text="$t('common-fields.label-price')"
        :invalid-amount="v$.labelPrice.amount.$error"
        :invalid-currency="v$.labelPrice.currency.$error"
        :errors-amount="v$.labelPrice.amount.$errors"
        :errors-currency="v$.labelPrice.currency.$errors"
        @blur:amount="v$.labelPrice.amount.$touch()"
        @blur:currency="v$.labelPrice.currency.$touch()"
        @update:model-value="$emit('update:labelPrice', $event)"
      />

      <MonetaryAmountInput
        id="paid-price"
        :model-value="paidPrice"
        :label-text="$t('common-fields.paid-price')"
        :invalid-amount="v$.paidPrice.amount.$error"
        :invalid-currency="v$.paidPrice.currency.$error"
        :errors-amount="v$.paidPrice.amount.$errors"
        :errors-currency="v$.paidPrice.currency.$errors"
        @blur:amount="v$.paidPrice.amount.$touch()"
        @blur:currency="v$.paidPrice.currency.$touch()"
        @update:model-value="$emit('update:paidPrice', $event)"
      />
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-2">
      <TextInput
        id="bought-at"
        :model-value="boughtAt ? convertUtcToLocalTimeZone(boughtAt) : ''"
        type="datetime-local"
        :label-text="$t('common-fields.bought-at')"
        @input="handleDateTimeInput($event, 'boughtAt')"
      />

      <TextInput
        id="billed-at"
        :model-value="billedAt ? convertUtcToLocalTimeZone(billedAt) : ''"
        type="datetime-local"
        :label-text="$t('common-fields.billed-at')"
        @input="handleDateTimeInput($event, 'billedAt')"
      />

      <TextInput
        id="arrived-at"
        :model-value="arrivedAt ? convertUtcToLocalTimeZone(arrivedAt) : ''"
        type="datetime-local"
        :label-text="$t('common-fields.arrived-at')"
        @input="handleDateTimeInput($event, 'arrivedAt')"
      />
    </div>

    <MarkdownInput
      id="notes"
      :model-value="notes ?? ''"
      rows="5"
      :label-text="$t('common-fields.notes')"
      :placeholder="$t('common-placeholders.book-notes')"
      @input="$emit('update:notes', $event.target.value)"
    />
  </div>
</template>
