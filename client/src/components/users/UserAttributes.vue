<script lang="ts" setup>
import type { UserEntity } from '@/types/tankobon-user'

export interface UserAttributesProps {
  user?: UserEntity | null
  loading?: boolean
}

const props = withDefaults(defineProps<UserAttributesProps>(), {
  user: undefined,
  loading: false,
})

const { user, loading } = toRefs(props)
const { t } = useI18n()

const metadata = computed(() => {
  const attributes = user.value?.attributes

  return [
    {
      title: t('common-fields.role'),
      value: attributes?.roles.includes('ROLE_ADMIN')
        ? t('user.role-admin')
        : t('user.role-user'),
    },
  ]
})
</script>

<template>
  <Block as="dl">
    <div class="space-y-5">
      <template v-for="(mt, i) in metadata">
        <div v-if="mt.value || loading" :key="i">
          <dt
            v-if="!loading"
            class="text-sm font-medium text-gray-950 dark:text-gray-100"
          >
            {{ mt.title }}
          </dt>
          <div v-else aria-hidden="true" class="skeleton w-16 h-5" />

          <dd
            v-if="!loading"
            class="mt-1 text-sm text-gray-700 dark:text-gray-300/90 inline-flex items-center"
          >
            <span>{{ mt.value }}</span>
          </dd>
          <div v-else aria-hidden="true" class="mt-1 skeleton w-32 h-6" />
        </div>
      </template>
    </div>
  </Block>
</template>
