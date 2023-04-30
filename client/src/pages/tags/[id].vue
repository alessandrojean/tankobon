<script lang="ts" setup>
import { PencilIcon, TrashIcon } from '@heroicons/vue/24/solid'
import { TagUpdate } from '@/types/tankobon-tag'
import { getRelationship } from '@/utils/api'

const { t } = useI18n()
const router = useRouter()
const tagId = useRouteParams<string | undefined>('id', undefined)
const notificator = useToaster()

const { 
  mutate: deleteTag,
  isLoading: isDeleting,
  isSuccess: isDeleted,
} = useDeleteTagMutation()
const { mutate: editTag, isLoading: isEditing } = useUpdateTagMutation()
const { data: tag, isLoading } = useTagQuery({
  tagId: tagId as Ref<string>,
  includes: ['library'],
  enabled: computed(() => {
    return !!tagId.value && !isDeleting.value && !isDeleted.value
  }),
  onError: async (error) => {
    await notificator.failure({
      title: t('tags.fetch-one-failure'),
      body: error.message,
    })
  }
})

const library = computed(() => getRelationship(tag.value, 'LIBRARY'))

function handleDelete() {
  deleteTag(tagId.value!, {
    onSuccess: async () => {
      notificator.success({ title: t('tags.deleted-with-success') })
      await router.replace({ name: 'tags' })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('tags.deleted-with-failure'),
        body: error.message,
      })
    }
  })
}

const showEditDialog = ref(false)

function handleEditTag(tag: TagUpdate) {
  editTag(tag, {
    onSuccess: async () => {
      await notificator.success({ title: t('tags.edited-with-success') })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('tags.edited-with-failure'),
        body: error.message,
      })
    }
  })
}
</script>

<template>
  <div>
    <Header
      :title="tag?.attributes.name ?? ''"
      :loading="isLoading"
      class="mb-3 md:mb-0"
    >
      <template #actions>
        <div class="flex space-x-2">
          <Button
            class="w-11 h-11"
            :loading="isEditing"
            :disabled="isDeleting"
            :title="$t('common-actions.edit')"
            @click="showEditDialog = true"
          >
            <span class="sr-only">{{ $t('common-actions.edit') }}</span>
            <PencilIcon class="w-6 h-6" />
          </Button>

          <Button
            class="w-11 h-11"
            kind="danger"
            :disabled="isEditing"
            :loading="isDeleting"
            :title="$t('common-actions.delete')"
            @click="handleDelete"
          >
            <span class="sr-only">{{ $t('common-actions.delete') }}</span>
            <TrashIcon class="w-6 h-6" />
          </Button>
        </div>
      </template>
    </Header>
    <div class="max-w-7xl mx-auto p-4 sm:p-6 space-y-10">
      <BlockMarkdown
        :loading="isLoading"
        :markdown="tag?.attributes?.description"
        :title="$t('common-fields.description')"
      />
    </div>

    <TagEditDialog
      v-if="tag"
      :is-open="showEditDialog"
      :tag-entity="tag"
      @submit="handleEditTag"
      @close="showEditDialog = false"
    />
  </div>
</template>

<route lang="yaml">
  meta:
    layout: dashboard
</route>
