<script lang="ts" setup>
import useVuelidate from '@vuelidate/core'
import { email, required, helpers } from '@vuelidate/validators'

import { EnvelopeIcon, IdentificationIcon, KeyIcon } from '@heroicons/vue/24/outline'
import { BookOpenIcon } from '@heroicons/vue/24/solid'
import type { ClaimAdmin } from '@/types/tankobon-claim'

const router = useRouter()
const { data: claimStatus, isFetched } = useServerClaimStatusQuery()
const { mutate: claimServer, isLoading } = useClaimServerMutation()

const formState = reactive<ClaimAdmin>({
  name: '',
  email: '',
  password: '',
})

const { t } = useI18n()
const messageRequired = helpers.withMessage(t('validation.required'), required)
const messageEmail = helpers.withMessage(t('validation.email'), email)

const rules = {
  name: { messageRequired },
  email: { messageRequired, messageEmail },
  password: { messageRequired },
}

const v$ = useVuelidate(rules, formState)

watch([claimStatus, isFetched], () => {
  if (claimStatus.value?.isClaimed) {
    router.replace({ name: 'sign-in' })
  }
})

async function handleSignIn() {
  const isFormValid = await v$.value.$validate()

  if (!isFormValid) {
    return
  }

  const admin: ClaimAdmin = toRaw(formState)

  claimServer(admin, {
    onSuccess: () => {
      router.replace({ name: 'sign-in' })
    }
  })
}
</script>

<template>
  <div class="bg-gray-100 min-h-screen flex flex-col items-center justify-center">
    <div>
      <BookOpenIcon class="w-12 h-12 text-primary-500" />
    </div>

    <h1 class="mt-6 font-display font-semibold text-3xl">
      {{ $t('claim-server.header') }}
    </h1>

    <p class="mt-1 text-sm text-gray-700">
      {{ $t('claim-server.summary') }}
    </p>

    <section class="mt-10 bg-white shadow rounded-xl w-full max-w-sm p-6">
      <form class="space-y-6" @submit.prevent="handleSignIn" novalidate>
        <div class="space-y-2">
          <TextInput
            v-model="v$.name.$model"
            id="name"
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
        <Button
          type="submit"
          kind="primary"
          class="w-full"
          :loading="isLoading"
        >
          {{ $t('common-actions.sign-up') }}
        </Button>
      </form>
    </section>
  </div>
</template>