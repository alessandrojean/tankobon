<script lang="ts" setup>
import useVuelidate from '@vuelidate/core'
import { email, required, helpers } from '@vuelidate/validators'

import { EnvelopeIcon, KeyIcon } from '@heroicons/vue/24/outline'
import { BookOpenIcon } from '@heroicons/vue/24/solid'
import { useUserStore } from '@/stores/user';

const formState = reactive({
  email: '',
  password: '',
  rememberMe: false,
})

const { t } = useI18n()
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
const { data: claimStatus, isFetched } = useServerClaimStatusQuery()
const { data: userLibraries, refetch: refetchUserLibraries } = useUserLibrariesQuery({
  enabled: computed(() => userStore.isAuthenticated)
})

watch([claimStatus, isFetched], () => {
  if (claimStatus.value?.isClaimed === false) {
    router.replace({ name: 'claim-server' })
  }
})

const isLoading = ref(false)
const error = ref<string>()

async function handleSignIn() {
  const isFormValid = await v$.value.$validate()

  if (!isFormValid) {
    return
  }

  isLoading.value = true

  try {
    await userStore.signIn({
      email: formState.email,
      password: formState.password
    })

    await refetchUserLibraries()

    if (userStore.isAuthenticated) {
      if (route.query.redirect) {
        await router.push({ path: route.query.redirect.toString() })
      } else if (userLibraries.value?.length === 0) {
        await router.push({ name: 'welcome' })
      } else {
        await router.push({ name: 'index' })
      }
    }
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="bg-gray-100 dark:bg-gray-900 min-h-screen flex flex-col items-center justify-center">
    <div>
      <BookOpenIcon class="w-12 h-12 text-primary-500" />
    </div>

    <h1 class="mt-6 dark:text-gray-100 font-display font-semibold text-3xl">
      {{ $t('sign-in.header') }}
    </h1>

    <p class="mt-1 dark:text-gray-400 text-sm text-gray-700">
      {{ $t('sign-in.new-accounts-info') }}
    </p>

    <section class="mt-10 bg-white dark:bg-block-dark shadow rounded-xl w-full max-w-sm p-6">
      <Alert
        class="mb-2 rounded-lg dark:!rounded-lg"
        type="error"
        :show="error !== undefined && !isLoading"
        :border="false"
      >
        <p>{{ error }}</p>
      </Alert>
      <form class="space-y-4" @submit.prevent="handleSignIn" novalidate>
        <div class="space-y-2">
          <TextInput
            v-model="v$.email.$model"
            id="email"
            type="email"
            :label-text="$t('common-fields.email')"
            auto-complete="email"
            :placeholder="$t('common-placeholders.email')"
            :invalid="v$.email.$error"
            :errors="v$.email.$errors"
            required
          >
            <template #left-icon>
              <EnvelopeIcon class="w-7 h-7 text-current" />
            </template>
          </TextInput>
          <TextInput
            v-model="v$.password.$model"
            id="password"
            type="password"
            :label-text="$t('common-fields.password')"
            auto-complete="current-password"
            :placeholder="$t('common-placeholders.password')"
            :invalid="v$.password.$error"
            :errors="v$.password.$errors"
            required
          >
            <template #left-icon>
              <KeyIcon class="w-7 h-7 text-current" />
            </template>
          </TextInput>
        </div>
        <CheckboxInput
          v-model="formState.rememberMe"
          id="remember-me"
          :label-text="$t('common-fields.remember-me')"
        />
        <Button
          type="submit"
          kind="primary"
          class="w-full"
          :loading="isLoading"
        >
          {{ $t('common-actions.sign-in') }}
        </Button>
      </form>
    </section>
  </div>
</template>