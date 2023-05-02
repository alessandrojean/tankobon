<script lang="ts" setup>
import useVuelidate from '@vuelidate/core'
import { email, helpers, required } from '@vuelidate/validators'

import { EnvelopeIcon, KeyIcon } from '@heroicons/vue/24/outline'
import { BookOpenIcon } from '@heroicons/vue/24/solid'
import { useUserStore } from '@/stores/user'

const formState = reactive({
  email: '',
  password: '',
  rememberMe: false,
})

const { t } = useI18n()
const notificator = useToaster()
const messageRequired = helpers.withMessage(t('validation.required'), required)
const messageEmail = helpers.withMessage(t('validation.email'), email)

const rules = {
  email: { messageRequired, messageEmail },
  password: { messageRequired },
}

const v$ = useVuelidate(rules, formState)

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const { data: claimStatus, isFetched } = useServerClaimStatusQuery({
  onError: async (error) => {
    await notificator.failure({
      title: t('claim-server.fetch-failure'),
      body: error.message,
    })
  },
})
const {
  data: userLibraries,
  refetch: refetchUserLibraries,
} = useUserLibrariesByUserQuery({
  userId: computed(() => userStore.me?.id ?? ''),
  enabled: computed(() => userStore.isAuthenticated),
  onError: async (error) => {
    await notificator.failure({
      title: t('libraries.fetch-failure'),
      body: error.message,
    })
  },
})

watch([claimStatus, isFetched], () => {
  if (claimStatus.value?.isClaimed === false)
    router.replace({ name: 'claim-server' })
})

const isLoading = ref(false)
const error = ref<string>()

async function handleSignIn() {
  const isFormValid = await v$.value.$validate()

  if (!isFormValid)
    return

  isLoading.value = true
  error.value = undefined

  try {
    await userStore.signIn({
      email: formState.email,
      password: formState.password,
    })

    await refetchUserLibraries()

    if (userStore.isAuthenticated) {
      if (userLibraries.value?.length === 0)
        await router.push({ name: 'welcome' })
      else if (route.query.redirect)
        await router.push({ path: route.query.redirect.toString() })
      else
        await router.push({ name: 'index' })
    }
  }
  catch (e) {
    error.value = (e as Error).message
  }
  finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="bg-gray-100 dark:bg-gray-950 min-h-screen flex flex-col items-center justify-center">
    <div>
      <BookOpenIcon class="w-12 h-12 text-primary-500" />
    </div>

    <h1 class="mt-6 dark:text-gray-100 font-display-safe font-semibold text-2xl sm:text-3xl">
      {{ $t('sign-in.header') }}
    </h1>

    <p class="mt-1 dark:text-gray-400 text-sm text-gray-700">
      {{ $t('sign-in.new-accounts-info') }}
    </p>

    <section class="mt-10 bg-white dark:bg-block-dark shadow sm:rounded-xl w-full max-w-sm p-6">
      <Alert
        class="mb-2 rounded-lg dark:!rounded-lg"
        type="error"
        :show="typeof error === 'string' && !isLoading"
        :border="false"
      >
        <p>{{ error }}</p>
      </Alert>
      <form class="space-y-4" novalidate @submit.prevent="handleSignIn">
        <div class="space-y-2">
          <TextInput
            id="email"
            v-model="formState.email"
            type="email"
            :label-text="$t('common-fields.email')"
            auto-complete="email"
            :placeholder="$t('common-placeholders.email').replace('[at]', '@')"
            :invalid="v$.email.$error"
            :errors="v$.email.$errors"
            required
            @blur="v$.email.$touch()"
          >
            <template #left-icon>
              <EnvelopeIcon class="w-7 h-7 text-current" />
            </template>
          </TextInput>
          <TextInput
            id="password"
            v-model="formState.password"
            type="password"
            :label-text="$t('common-fields.password')"
            auto-complete="current-password"
            :placeholder="$t('common-placeholders.password')"
            :invalid="v$.password.$error"
            :errors="v$.password.$errors"
            required
            @blur="v$.password.$touch()"
          >
            <template #left-icon>
              <KeyIcon class="w-7 h-7 text-current" />
            </template>
          </TextInput>
        </div>
        <CheckboxInput
          id="remember-me"
          v-model="formState.rememberMe"
          :label-text="$t('common-fields.remember-me')"
        />
        <Button
          type="submit"
          kind="primary"
          class="w-full"
          size="large"
          :loading="isLoading"
        >
          {{ $t('common-actions.sign-in') }}
        </Button>
      </form>
    </section>
  </div>
</template>
