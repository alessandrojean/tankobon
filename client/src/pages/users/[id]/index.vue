<script lang="ts" setup>
import { BuildingLibraryIcon, InformationCircleIcon, KeyIcon } from '@heroicons/vue/20/solid'
import { UserIcon } from '@heroicons/vue/24/outline'
import { PencilIcon, TrashIcon } from '@heroicons/vue/24/solid'
import { getRelationship } from '@/utils/api'
import type { PillTab } from '@/components/PillTabsList.vue'
import { safeNumber } from '@/utils/route'
import type { LibrarySort } from '@/types/tankobon-library'
import type { Sort } from '@/types/tankobon-api'
import type { AuthenticationActivitySort } from '@/types/tankobon-authentication-activity'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const userIdRoute = useRouteParams<string | undefined>('id', undefined)
const userStore = useUserStore()
const notificator = useToaster()

const userId = computed(() => {
  return userIdRoute.value === 'me' ? userStore.me?.id : userIdRoute.value
})

const { mutate: deleteUser, isLoading: isDeleting, isSuccess: isDeleted } = useDeleteUserMutation()

const queryEnabled = computed(() => {
  return !!userId.value && !isDeleting.value && !isDeleted.value && route.name === 'users-id'
})

const { data: user, isLoading } = useUserQuery({
  userId: userId as Ref<string>,
  enabled: queryEnabled,
  onError: async (error) => {
    await notificator.failure({
      title: t('users.fetch-one-failure'),
      body: error.message,
    })
  },
})

const sortLibraries = ref<Sort<LibrarySort> | null>({ property: 'name', direction: 'asc' })

const { data: libraries, isLoading: isLoadingLibraries } = useUserLibrariesByUserQuery({
  userId: userId as Ref<string>,
  includeShared: true,
  sort: computed(() => sortLibraries.value ? [sortLibraries.value] : undefined),
  enabled: computed(() => queryEnabled.value && !!user.value?.id),
  keepPreviousData: true,
  onError: async (error) => {
    await notificator.failure({
      title: t('libraries.fetch-failure'),
      body: error.message,
    })
  },
})

const sortAuthentication = ref<Sort<AuthenticationActivitySort> | null>()

const { data: authenticationActivity, isLoading: isLoadingAuthentication } = useUserAuthenticationActivityQuery({
  userId: userId as Ref<string>,
  sort: computed(() => sortAuthentication.value ? [sortAuthentication.value] : undefined),
  enabled: computed(() => queryEnabled.value && !!user.value?.id),
  keepPreviousData: true,
  onError: async (error) => {
    await notificator.failure({
      title: t('libraries.fetch-failure'),
      body: error.message,
    })
  },
})

function handleDelete() {
  deleteUser(userId.value!, {
    onSuccess: async () => {
      notificator.success({ title: t('users.deleted-with-success') })
      await router.replace({ name: 'users' })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('users.deleted-with-failure'),
        body: error.message,
      })
    },
  })
}

useHead({ title: () => user.value?.attributes.name ?? '' })

const tabs: PillTab[] = [
  { key: '0', text: 'users.information', icon: InformationCircleIcon },
  { key: '1', text: 'entities.libraries', icon: BuildingLibraryIcon },
  { key: '2', text: 'authentication-activity.header-short', icon: KeyIcon },
]

const activeTabHash = useRouteQuery('tab', '0', { transform: v => safeNumber(v, 0, { min: 0, max: tabs.length - 1 }) })
const activeTab = computed({
  get: () => {
    const index = Number(activeTabHash.value)
    return tabs[index] ?? tabs[0]
  },
  set: newTab => activeTabHash.value = Number(newTab.key),
})
</script>

<template>
  <div
    :class="[
      'bg-white dark:bg-gray-950 motion-safe:transition-colors',
      'duration-300 ease-in-out -mt-16 relative',
    ]"
  >
    <div class="absolute inset-x-0 top-0">
      <ImageBanner
        class="!h-52"
        :alt="user?.attributes.name ?? ''"
        :loading="isLoading"
        :image="getRelationship(user, 'AVATAR')?.attributes"
        size="64"
        kind="repeated"
      />
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 z-10 pt-20 pb-6 relative">
      <TabGroup
        as="div"
        class="user-grid"
        :selected-index="Number(activeTab.key)"
        @change="activeTab = tabs[$event]"
      >
        <ImageCover
          class="user-avatar"
          version="256"
          aspect-ratio="1 / 1"
          :icon="UserIcon"
          :loading="isLoading"
          :image="getRelationship(user, 'AVATAR')?.attributes"
          :alt="user?.attributes.name ?? ''"
        />

        <UserName
          class="user-name"
          :loading="isLoading"
          :user="user"
        />

        <div class="user-buttons pt-1.5 flex items-center justify-between">
          <PillTabsList
            v-model="activeTab"
            :tabs="tabs"
            :disabled="isLoading"
          />

          <div
            v-if="isLoading"
            class="flex justify-center sm:justify-start items-center gap-2"
          >
            <div class="skeleton w-12 h-12" />
            <div class="skeleton w-12 h-12" />
          </div>
          <Toolbar v-else class="flex justify-center sm:justify-start items-center gap-2">
            <Button
              class="aspect-1"
              size="small"
              is-router-link
              :to="{ name: 'users-id-edit', params: { id: user?.id } }"
              :disabled="isDeleting"
              :title="$t('common-actions.edit')"
            >
              <span class="sr-only">{{ $t('common-actions.edit') }}</span>
              <PencilIcon class="w-5 h-5" />
            </Button>

            <Button
              class="aspect-1"
              size="small"
              :loading="isDeleting"
              :title="$t('common-actions.delete')"
              @click="handleDelete"
            >
              <span class="sr-only">{{ $t('common-actions.delete') }}</span>
              <TrashIcon class="w-5 h-5" />
            </Button>
          </Toolbar>
        </div>

        <TabPanels class="user-tabs">
          <TabPanel class="flex flex-col gap-4 sm:gap-6 -mb-4 sm:mb-0" :unmount="false">
            <BlockMarkdown
              :loading="isLoading"
              :markdown="user?.attributes?.biography"
              :title="$t('common-fields.biography')"
            />
          </TabPanel>

          <TabPanel :unmount="false">
            <LibrariesListViewer
              v-model:sort="sortLibraries"
              column-order-key="user_libraries_column_order"
              column-visibility-key="user_libraries_column_visibility"
              :show-new-button="false"
              :libraries="libraries"
              :loading="isLoadingLibraries || isLoading"
            />
          </TabPanel>

          <TabPanel :unmount="false">
            <UserAuthenticationActivityListViewer
              v-model:sort="sortAuthentication"
              column-order-key="user_authentication_activity_column_order"
              column-visibility-key="user_authentication_activity_column_visibility"
              :authentication-activity="authenticationActivity"
              :loading="isLoadingAuthentication || isLoading"
            />
          </TabPanel>
        </TabPanels>

        <div class="user-attributes">
          <UserAttributes
            class="sm:sticky sm:top-24"
            :loading="isLoading"
            :user="user"
          />
        </div>
      </TabGroup>
    </div>
  </div>
</template>

<route lang="yaml">
meta:
  layout: dashboard
  isAdminOnly: true
  transparentNavbar: true
</route>

<style lang="postcss">
.user-grid {
  display: grid;
  gap: 1rem;
  grid-template-areas:
    'avatar name'
    'buttons buttons'
    'tabs tabs'
    'attributes attributes';
  grid-template-columns: 6rem 1fr;

  @media (min-width: theme('screens.sm')) {
    gap: 1.5rem;
    grid-template-areas:
      'avatar name'
      'avatar buttons'
      'avatar padding'
      'attributes tabs';
    grid-template-columns: 12rem 1fr;
  }

  .user-avatar {
    grid-area: avatar / avatar / avatar / avatar;
  }

  .user-buttons {
    grid-area: buttons / buttons / buttons / buttons;
  }

  .user-name {
    grid-area: name / name / name / name;
  }

  .user-tabs {
    grid-area: tabs / tabs / tabs / tabs;
  }

  .user-attributes {
    grid-area: attributes / attributes / attributes / attributes;
  }
}
</style>
