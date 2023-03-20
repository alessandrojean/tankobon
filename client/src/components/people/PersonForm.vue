<script lang="ts" setup>
import { useVuelidate } from '@vuelidate/core'
import { helpers, required } from '@vuelidate/validators'

export interface PersonFormProps {
  name: string,
  description: string,
  mode?: 'creation' | 'update',
}

export type PersonFormEmits = {
  (e: 'update:name', name: string): void,
  (e: 'update:description', description: string): void,
  (e: 'validate', isValid: boolean): void,
}

const props = withDefaults(defineProps<PersonFormProps>(), {
  mode: 'creation',
})
const emit = defineEmits<PersonFormEmits>()

const { name, description, mode } = toRefs(props)

const { t } = useI18n()

const rules = computed(() => {
  const messageRequired = helpers.withMessage(t('validation.required'), required)

  return {
    name: { messageRequired }
  }
})

const v$ = useVuelidate(rules, { name })

watch(() => v$.value.$error, (isValid) => emit('validate', isValid))

defineExpose({ v$ })
</script>

<template>
  <div class="space-y-2">
    <TextInput
      :model-value="name"
      id="name"
      required
      :label-text="$t('common-fields.name')"
      :placeholder="$t('common-placeholders.person-name')"
      :invalid="v$.name.$error"
      :errors="v$.name.$errors"
      @blur="v$.name.$touch()"
      @input="$emit('update:name', $event.target.value)"
    />

    <TextAreaInput
      :model-value="description"
      id="description"
      rows="5"
      :label-text="$t('common-fields.description')"
      :placeholder="$t('common-placeholders.person-description')"
      @input="$emit('update:description', $event.target.value)"
    />
  </div>
</template>
