<script lang="ts" setup>
import useVuelidate from '@vuelidate/core'
import { helpers, required } from '@vuelidate/validators'

import { BookOpenIcon } from '@heroicons/vue/24/solid'
import type { LibraryCreation } from '@/types/tankobon-library'

const userStore = useUserStore()
const userId = computed(() => userStore.me?.id)
const router = useRouter()
const notificator = useToaster()
const { t } = useI18n()

const { data: libraries, refetch: refetchLibraries } = useUserLibrariesByUserQuery({
  unpaged: true,
  userId: userId as ComputedRef<string>,
  enabled: computed(() => userStore.isAuthenticated),
  select: response => response.data,
  onError: async (error) => {
    await notificator.failure({
      title: t('libraries.fetch-failure'),
      body: error.message,
    })
  },
})
const { mutate: createLibrary, isLoading, error } = useCreateLibraryMutation()

const hasNoLibraries = computed(() => {
  return libraries.value?.length !== undefined
    && libraries.value?.length === 0
})

watch(hasNoLibraries, (hasNoLibraries) => {
  if (!hasNoLibraries) {
    router.replace({ name: 'index' })
  }
})

onBeforeMount(async () => {
  await refetchLibraries()

  if (!hasNoLibraries.value) {
    router.replace({ name: 'index' })
  }
})

const formState = reactive<LibraryCreation>({
  name: '',
  description: '',
})
const messageRequired = helpers.withMessage(t('validation.required'), required)

const rules = {
  name: { messageRequired },
}

const v$ = useVuelidate(rules, formState)
const libraryStore = useLibraryStore()

async function handleCreateLibrary() {
  const isFormValid = await v$.value.$validate()

  if (!isFormValid) {
    return
  }

  const library = toRaw<LibraryCreation>(formState)

  createLibrary(library, {
    onSuccess: async (libraryCreated) => {
      libraryStore.setLibrary(libraryCreated)
      await router.replace({ name: 'index' })
    },
  })
}
</script>

<template>
  <div class="bg-gray-100 dark:bg-gray-950 min-h-screen flex flex-col items-center justify-center">
    <div>
      <BookOpenIcon class="w-12 h-12 text-primary-500" />
    </div>

    <h1 class="mt-6 dark:text-gray-100 font-display-safe font-semibold text-2xl sm:text-3xl">
      {{ $t('welcome.header') }}
    </h1>

    <p class="mt-1 dark:text-gray-400 text-sm text-gray-700">
      {{ $t('welcome.summary') }}
    </p>

    <section class="mt-10 bg-white dark:bg-block-dark shadow sm:rounded-xl w-full max-w-md p-6">
      <Alert
        class="mb-2 rounded-lg dark:!rounded-lg"
        type="error"
        :show="error?.message !== undefined && !isLoading"
        :border="false"
      >
        <p>{{ error?.message }}</p>
      </Alert>

      <form class="space-y-4" novalidate @submit.prevent="handleCreateLibrary">
        <div class="space-y-2">
          <TextInput
            id="name"
            v-model="v$.name.$model"
            :label-text="$t('common-fields.name')"
            :placeholder="$t('common-placeholders.library-name')"
            :invalid="v$.name.$error"
            :errors="v$.name.$errors"
            required
          />
          <TextAreaInput
            id="description"
            v-model="formState.description"
            class="resize-none"
            rows="5"
            :label-text="$t('common-fields.description')"
            :placeholder="$t('common-placeholders.library-description')"
          />
        </div>
        <Button
          type="submit"
          kind="primary"
          class="w-full"
          size="large"
          :loading="isLoading"
          :disabled="!hasNoLibraries"
        >
          {{ $t('common-actions.create-library') }}
        </Button>
      </form>
    </section>
  </div>
</template>
