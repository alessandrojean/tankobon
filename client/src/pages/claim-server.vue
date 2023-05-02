<script lang="ts" setup>
import useVuelidate from '@vuelidate/core'
import { email, helpers, required } from '@vuelidate/validators'

import { EnvelopeIcon, IdentificationIcon, KeyIcon } from '@heroicons/vue/24/outline'
import { BookOpenIcon } from '@heroicons/vue/24/solid'
import type { ClaimAdmin } from '@/types/tankobon-claim'

const notificator = useToaster()
const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const { data: claimStatus, isFetched } = useServerClaimStatusQuery({
  onError: async (error) => {
    await notificator.failure({
      title: t('claim-server.fetch-failure'),
      body: error.message,
    })
  },
})
const { mutateAsync: claimServer } = useClaimServerMutation()

const error = ref<Error | null>(null)
const isLoading = ref(false)

const formState = reactive<ClaimAdmin>({
  name: '',
  email: '',
  password: '',
})

const messageRequired = helpers.withMessage(t('validation.required'), required)
const messageEmail = helpers.withMessage(t('validation.email'), email)

const rules = {
  name: { messageRequired },
  email: { messageRequired, messageEmail },
  password: { messageRequired },
}

const v$ = useVuelidate(rules, formState)

watch([claimStatus, isFetched], () => {
  if (claimStatus.value?.isClaimed && !userStore.isAuthenticated && !isLoading.value)
    router.replace({ name: 'sign-in' })
})

async function handleSignIn() {
  const isFormValid = await v$.value.$validate()

  if (!isFormValid)
    return

  isLoading.value = true
  error.value = null
  const admin: ClaimAdmin = toRaw(formState)

  try {
    await claimServer(admin)
    await userStore.signIn({ email: admin.email, password: admin.password })
    await router.replace({ name: 'welcome' })
  }
  catch (e) {
    error.value = (e instanceof Error) ? e : error.value
  }
  finally {
    isLoading.value = false
  }
}

const passwordFocused = ref(false)
</script>

<template>
  <div class="bg-gray-100 dark:bg-gray-950 min-h-screen flex flex-col items-center justify-center">
    <div>
      <BookOpenIcon class="w-12 h-12 text-primary-500" />
    </div>

    <h1 class="mt-6 dark:text-gray-100 font-display-safe font-semibold text-2xl sm:text-3xl">
      {{ $t('claim-server.header') }}
    </h1>

    <p class="mt-1 dark:text-gray-400 text-sm text-gray-700">
      {{ $t('claim-server.summary') }}
    </p>

    <section class="mt-10 bg-white dark:bg-block-dark shadow sm:rounded-xl w-full max-w-sm p-6">
      <Alert
        class="mb-2 rounded-lg dark:!rounded-lg"
        type="error"
        :show="error?.message !== undefined && !isLoading"
        :border="false"
      >
        <p>{{ error?.message }}</p>
      </Alert>

      <form class="space-y-6" novalidate @submit.prevent="handleSignIn">
        <div class="space-y-2">
          <TextInput
            id="name"
            v-model="v$.name.$model"
            :label-text="$t('common-fields.name')"
            auto-complete="name"
            :placeholder="$t('common-placeholders.name')"
            :invalid="v$.name.$error"
            :errors="v$.name.$errors"
            required
          >
            <template #left-icon>
              <IdentificationIcon class="w-7 h-7 text-current" />
            </template>
          </TextInput>
          <TextInput
            id="email"
            v-model="v$.email.$model"
            type="email"
            :label-text="$t('common-fields.email')"
            auto-complete="email"
            :placeholder="$t('common-placeholders.email').replace('[at]', '@')"
            :invalid="v$.email.$error"
            :errors="v$.email.$errors"
            required
          >
            <template #left-icon>
              <EnvelopeIcon class="w-7 h-7 text-current" />
            </template>
          </TextInput>
          <TextInput
            id="password"
            v-model="v$.password.$model"
            type="password"
            :label-text="$t('common-fields.password')"
            auto-complete="current-password"
            :placeholder="$t('common-placeholders.password')"
            :invalid="v$.password.$error"
            :errors="v$.password.$errors"
            required
            @focus="passwordFocused = true"
            @blur="passwordFocused = false"
          >
            <template #left-icon>
              <KeyIcon class="w-7 h-7 text-current" />
            </template>
            <template #footer>
              <FadeTransition>
                <PasswordStrength
                  v-if="formState.password.length > 0 || passwordFocused"
                  class="mt-2"
                  :minimum-length="8"
                  :password="formState.password"
                />
              </FadeTransition>
            </template>
          </TextInput>
        </div>
        <Button
          type="submit"
          kind="primary"
          class="w-full"
          size="large"
          :loading="isLoading"
          :disabled="claimStatus?.isClaimed"
        >
          {{ $t('common-actions.sign-up') }}
        </Button>
      </form>
    </section>
  </div>
</template>
