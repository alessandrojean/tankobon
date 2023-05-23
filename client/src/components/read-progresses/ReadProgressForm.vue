<script lang="ts" setup>
import { useVuelidate } from '@vuelidate/core'
import { helpers, maxValue, minValue, or } from '@vuelidate/validators'
import { convertLocalTimeZoneToUtc, convertUtcToLocalTimeZone } from '@/utils/date'

export interface ReadProgressFormProps {
  startedAt: string | null
  finishedAt: string | null
  page: number
  pageCount: number
  isCompleted: boolean
  mode?: 'creation' | 'update'
}

export interface ReadProgressFormEmits {
  (e: 'update:startedAt', startedAt: string | null): void
  (e: 'update:finishedAt', finishedAt: string | null): void
  (e: 'update:page', page: number): void
  (e: 'update:isCompleted', isCompleted: boolean): void
  (e: 'validate', isValid: boolean): void
}

const props = withDefaults(defineProps<ReadProgressFormProps>(), {
  mode: 'creation',
})
const emit = defineEmits<ReadProgressFormEmits>()

const { startedAt, finishedAt, page, pageCount, isCompleted } = toRefs(props)

const startedAtDate = computed(() => startedAt.value ? new Date(startedAt.value) : 0)
const finishedAtDate = computed(() => finishedAt.value ? new Date(finishedAt.value) : 0)

const { t } = useI18n()

const rules = computed(() => {
  const messageMinValue = helpers.withMessage(({ $params }) => t('validation.min-value', [$params.min]), minValue(0))
  const messageMaxValue = helpers.withMessage(({ $params }) => t('validation.max-value', [$params.max]), maxValue(pageCount))

  const validFinishedAt = or(
    (value: Date | number) => typeof value === 'number' && value === 0,
    () => startedAt.value === null,
    // @ts-expect-error The library forgot to add the Date in the types.
    minValue(startedAtDate),
  )

  const validStartedAt = or(
    (value: Date | number) => typeof value === 'number' && value === 0,
    () => finishedAt.value === null,
    // @ts-expect-error The library forgot to add the Date in the types.
    maxValue(finishedAtDate),
  )

  const messageStart = helpers.withMessage(t('validation.durational-start'), validStartedAt)
  const messageFinish = helpers.withMessage(t('validation.durational-finish'), validFinishedAt)

  return {
    startedAt: { messageStart },
    finishedAt: { messageFinish },
    page: { messageMinValue, messageMaxValue },
  }
})

const v$ = useVuelidate(rules, {
  startedAt: startedAtDate,
  finishedAt: finishedAtDate,
  page,
})

watch(() => v$.value.$error, isValid => emit('validate', isValid))

defineExpose({ v$ })

async function handleDateTimeInput(event: KeyboardEvent, field: 'startedAt' | 'finishedAt') {
  const input = event.target as HTMLInputElement
  const value = input.value.length > 0
    ? convertLocalTimeZoneToUtc(input.value)
    : null

  if (field === 'startedAt') {
    emit('update:startedAt', value)
  } else if (field === 'finishedAt') {
    emit('update:finishedAt', value)
    await nextTick()

    if (!v$.value.finishedAt.$invalid && value) {
      emit('update:isCompleted', true)
      emit('update:page', pageCount.value)
    }
  }
}

whenever(isCompleted, () => emit('update:page', pageCount.value))
</script>

<template>
  <div class="flex flex-col gap-2">
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
      <TextInput
        id="started-at"
        :model-value="startedAt ? convertUtcToLocalTimeZone(startedAt) : ''"
        type="datetime-local"
        :label-text="$t('common-fields.started-at')"
        :invalid="v$.startedAt.$error"
        :errors="v$.startedAt.$errors"
        @blur="v$.startedAt.$touch()"
        @input="handleDateTimeInput($event, 'startedAt')"
      />

      <TextInput
        id="finished-at"
        :model-value="finishedAt ? convertUtcToLocalTimeZone(finishedAt) : ''"
        type="datetime-local"
        :label-text="$t('common-fields.finished-at')"
        :invalid="v$.finishedAt.$error"
        :errors="v$.finishedAt.$errors"
        @blur="v$.finishedAt.$touch()"
        @input="handleDateTimeInput($event, 'finishedAt')"
      />
    </div>

    <RangeInput
      id="page"
      :min="0"
      :max="pageCount"
      :model-value="page"
      :placeholder="String(pageCount)"
      :label-text="$t('common-fields.page')"
      :invalid="v$.page.$error"
      :errors="v$.page.$errors"
      @blur="v$.page.$touch()"
      @update:model-value="$emit('update:page', $event)"
    />

    <div class="mt-4">
      <CheckboxInput
        id="is-completed"
        :model-value="isCompleted"
        :label-text="$t('read-progresses.completed')"
        @update:model-value="$emit('update:isCompleted', $event)"
      />
    </div>
  </div>
</template>
