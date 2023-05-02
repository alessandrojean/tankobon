<script lang="ts" setup>
import { useVuelidate } from '@vuelidate/core'
import { helpers, required } from '@vuelidate/validators'

export interface BookMetadataFormProps {
  code: string,
  title: string,
  synopsis: string,
  notes: string,
  mode?: 'creation' | 'update',
}

export type BookMetadataFormEmits = {
  (e: 'update:code', code: string): void,
  (e: 'update:title', title: string): void,
  (e: 'update:synopsis', synopsis: string): void,
  (e: 'update:notes', notes: string): void,
  (e: 'validate', isValid: boolean): void,
}

const props = withDefaults(defineProps<BookMetadataFormProps>(), {
  mode: 'creation',
})
const emit = defineEmits<BookMetadataFormEmits>()

const { code, title, mode } = toRefs(props)

const { t } = useI18n()

const rules = computed(() => {
  const messageRequired = helpers.withMessage(t('validation.required'), required)

  return {
    code: { messageRequired },
    title: { messageRequired },
  }
})

const v$ = useVuelidate(rules, { code, title })

watch(() => v$.value.$error, (isValid) => emit('validate', isValid))

defineExpose({ v$ })
</script>

<template>
  <div class="space-y-2">
    <TextInput
      :model-value="code"
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
      :model-value="title"
      id="title"
      required
      :label-text="$t('common-fields.title')"
      :placeholder="$t('common-placeholders.book-title')"
      :invalid="v$.title.$error"
      :errors="v$.title.$errors"
      @blur="v$.title.$touch()"
      @input="$emit('update:title', $event.target.value)"
    />

    <MarkdownInput
      :model-value="synopsis"
      id="synopsis"
      rows="10"
      :label-text="$t('common-fields.synopsis')"
      :placeholder="$t('common-placeholders.book-synopsis')"
      @input="$emit('update:synopsis', $event.target.value)"
    />

    <MarkdownInput
      :model-value="notes"
      id="notes"
      rows="5"
      :label-text="$t('common-fields.notes')"
      :placeholder="$t('common-placeholders.book-notes')"
      @input="$emit('update:notes', $event.target.value)"
    />
  </div>
</template>
