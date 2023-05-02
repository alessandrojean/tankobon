<script lang="ts" setup>
import { useVuelidate } from '@vuelidate/core'
import { email as emailValidator, helpers, required } from '@vuelidate/validators'
import { ArrowPathIcon } from '@heroicons/vue/20/solid'
import { emailIsAvailable, emailIsAvailableIfNotSame } from '@/utils/validation'
import type { UserRole } from '@/types/tankobon-user'

export interface UserFormProps {
  name: string
  email: string
  hidePassword?: boolean
  password?: string
  hideRoles?: boolean
  roles: UserRole[]
  hideBiography?: boolean
  biography?: string
  mode?: 'creation' | 'update'
}

export interface UserFormEmits {
  (e: 'click:regenerate-password'): void
  (e: 'update:name', name: string): void
  (e: 'update:email', email: string): void
  (e: 'update:password', password: string): void
  (e: 'update:roles', roles: UserRole[]): void
  (e: 'update:biography', biography: ''): void
  (e: 'validate', isValid: boolean): void
}

const props = withDefaults(defineProps<UserFormProps>(), {
  hideBiography: false,
  hidePassword: false,
  hideRoles: false,
  mode: 'creation',
})
const emit = defineEmits<UserFormEmits>()

const { name, email, password, roles, mode, hidePassword } = toRefs(props)

const isAdmin = computed({
  get: () => roles.value.includes('ROLE_ADMIN'),
  set: isAdmin => emit('update:roles', isAdmin ? ['ROLE_ADMIN'] : ['ROLE_USER']),
})

const { t } = useI18n()

const rules = computed(() => {
  const messageRequired = helpers.withMessage(t('validation.required'), required)
  const messageEmail = helpers.withMessage(t('validation.email'), emailValidator)

  return {
    name: { messageRequired },
    email: {
      messageRequired,
      messageEmail,
      availableIfNotSame: helpers.withMessage(
        t('validation.available'),
        mode.value === 'creation' ? emailIsAvailable : emailIsAvailableIfNotSame(email.value),
      ),
    },
    password: { required: hidePassword.value ? () => true : messageRequired },
  }
})

const v$ = useVuelidate(rules, {
  name,
  email,
  password,
})

const passwordFocused = ref(false)

watch(() => v$.value.$error, isValid => emit('validate', isValid))

defineExpose({ v$ })
</script>

<template>
  <div class="space-y-4">
    <div class="space-y-2">
      <TextInput
        id="name"
        :model-value="name"
        required
        :label-text="$t('common-fields.name')"
        :placeholder="$t('common-placeholders.name')"
        :invalid="v$.name.$error"
        :errors="v$.name.$errors"
        @blur="v$.name.$touch()"
        @input="$emit('update:name', $event.target.value)"
      />

      <TextInput
        id="email"
        :model-value="email"
        required
        :label-text="$t('common-fields.email')"
        :placeholder="$t('common-placeholders.email').replace('[at]', '@')"
        :invalid="v$.email.$error"
        :errors="v$.email.$errors"
        @blur="v$.email.$touch()"
        @input="$emit('update:email', $event.target.value)"
      />

      <TextAreaInput
        v-if="!hideBiography && biography !== undefined"
        id="biography"
        :model-value="biography"
        rows="3"
        :label-text="$t('common-fields.biography')"
        :placeholder="$t('common-placeholders.user-biography')"
        @input="$emit('update:biography', $event.target.value)"
      />

      <TextInput
        v-if="!hidePassword && password !== undefined"
        id="password"
        :model-value="password"
        class="font-mono"
        required
        :label-text="$t('common-fields.password')"
        :placeholder="$t('common-placeholders.password')"
        :invalid="v$.password.$error"
        :errors="v$.password.$errors"
        @input="$emit('update:password', $event.target.value)"
        @focus="passwordFocused = true"
        @blur="passwordFocused = false; v$.password.$touch()"
      >
        <template #right-icon>
          <Button
            class="w-10 h-10 -mr-2"
            kind="ghost-alt"
            :title="$t('common-actions.regenerate-password')"
            @click="$emit('click:regenerate-password')"
          >
            <span class="sr-only">{{ $t('common-actions.regenerate-password') }}</span>
            <ArrowPathIcon class="w-5 h-5" />
          </Button>
        </template>
        <template #footer>
          <FadeTransition>
            <PasswordStrength
              v-if="password.length > 0 || passwordFocused"
              class="mt-2"
              :minimum-length="8"
              :password="password"
            />
          </FadeTransition>
        </template>
      </TextInput>
    </div>

    <CheckboxInput
      v-if="!hideRoles"
      id="isAdmin"
      v-model="isAdmin"
      :label-text="$t('user.role-admin')"
    />
  </div>
</template>
