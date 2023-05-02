<script lang="ts" setup>
import { DimensionsString } from '@/types/tankobon-dimensions'
import { convertLocalTimeZoneToUtc, convertUtcToLocalTimeZone } from '@/utils/date'
import { positiveDecimal } from '@/utils/validation'
import { useVuelidate } from '@vuelidate/core'
import { helpers, required, integer, minValue } from '@vuelidate/validators'

export interface BookMetadataFormProps {
  notes: string,
  billedAt: string | null | undefined,
  boughtAt: string | null | undefined,
  arrivedAt: string | null | undefined,
  mode?: 'creation' | 'update',
}

export type BookMetadataFormEmits = {
  (e: 'update:notes', notes: string): void,
  (e: 'update:billedAt', billedAt: string): void,
  (e: 'update:arrivedAt', arrivedAt: string): void,
  (e: 'update:boughtAt', boughtAt: string): void,
  (e: 'validate', isValid: boolean): void,
}

const props = withDefaults(defineProps<BookMetadataFormProps>(), {
  mode: 'creation',
})
const emit = defineEmits<BookMetadataFormEmits>()

const {  } = toRefs(props)

const { t } = useI18n()

// const rules = computed(() => {
//   const messageRequired = helpers.withMessage(t('validation.required'), required)
//   const messageInteger = helpers.withMessage(t('validation.integer'), integer)
//   const messageMinValue = helpers.withMessage(({ $params }) => t('validation.min-value', [$params.min]), minValue(0))
//   const messageDecimal = helpers.withMessage(t('validation.decimal'), positiveDecimal)

//   return {
//     code: { messageRequired },
//     title: { messageRequired },
//     pageCount: { messageInteger, messageMinValue },
//     dimensions: {
//       widthCm: { messageDecimal },
//       heightCm: { messageDecimal },
//     }
//   }
// })

// const v$ = useVuelidate(rules, { code, title, pageCount, dimensions })

// watch(() => v$.value.$error, (isValid) => emit('validate', isValid))

// defineExpose({ v$ })

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
