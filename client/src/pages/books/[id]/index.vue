<script lang="ts" setup>
import { isIsbnCode } from '@/types/tankobon-book'
import { createFlagUrl } from '@/utils/flags'

const { t, locale } = useI18n()
const bookId = useRouteParams<string | undefined>('id', undefined)
const notificator = useToaster()

// const { mutate: deletePublisher, isLoading: isDeleting, isSuccess: isDeleted } = useDeletePublisherMutation()
// const { mutate: editPublisher, isLoading: isEditing } = useUpdatePublisherMutation()

const { data: book, isLoading } = useBookQuery({
  bookId: bookId as Ref<string>,
  includes: [
    'publisher',
    'collection',
    'contributor',
    'series',
    'store',
    'tag',
    'library',
    'cover_art',
  ],
  enabled: computed(() => !!bookId.value), // && !isDeleting.value && !isDeleted.value),
  onError: async (error) => {
    await notificator.failure({
      title: t('books.fetch-one-failure'),
      body: error.message,
    })
  },
})

const { data: contributors, isLoading: isLoadingContributors } = useBookContributorsQuery({
  bookId: bookId as Ref<string>,
  includes: ['person_picture'],
  select: response => response.data,
  enabled: computed(() => !!bookId.value && !isLoading.value), // && !isDeleting.value && !isDeleted.value),
  onError: async (error) => {
    await notificator.failure({
      title: t('book-contributors.fetch-failure'),
      body: error.message,
    })
  },
})

const showBookInfo = computed(() => {
  return !isLoading.value && !!book.value
})

// function handleDelete() {
//   deletePublisher(publisherId.value!, {
//     onSuccess: async () => {
//       notificator.success({ title: t('publishers.deleted-with-success') })
//       await router.replace({ name: 'publishers' })
//     },
//     onError: async (error) => {
//       await notificator.failure({
//         title: t('publishers.deleted-with-failure'),
//         body: error.message,
//       })
//     }
//   })
// }

// const showEditDialog = ref(false)

// function handleEditPublisher(publisher: PublisherUpdate) {
//   editPublisher(publisher, {
//     onSuccess: async () => {
//       await notificator.success({ title: t('publishers.edited-with-success') })
//     },
//     onError: async (error) => {
//       await notificator.failure({
//         title: t('publishers.edited-with-failure'),
//         body: error.message,
//       })
//     }
//   })
// }

const regionCode = computed(() => {
  const code = book.value?.attributes?.code
  return isIsbnCode(code) ? code.region : null
})

const regionName = computed(() => {
  if (regionCode.value === null) {
    return null
  }

  const formatter = new Intl.DisplayNames(locale.value, { type: 'region' })

  return formatter.of(regionCode.value!)
})

const flagUrl = computed(() => {
  return regionCode.value ? createFlagUrl(regionCode.value, 'rectangle') : null
})

useHead({ title: () => book.value?.attributes?.title ?? '' })
</script>

<template>
  <div
    class="bg-white dark:bg-gray-950 motion-safe:transition-colors duration-300 ease-in-out -mt-16 relative"
  >
    <div class="absolute inset-x-0 top-0">
      <BookBanner :loading="!showBookInfo" :book="book" />
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 z-10 pt-20 pb-6 relative">
      <div class="book-grid">
        <BookCover
          class="book-cover"
          :loading="!showBookInfo"
          :book="book"
        >
          <img
            v-if="flagUrl"
            :src="flagUrl"
            :alt="$t('common.flag', [regionName])"
            class="inline-block z-10 w-5 sm:w-6 aspect-[3/2] rounded-sm shadow absolute right-1.5 sm:right-3 bottom-1.5 sm:bottom-3 pointer-events-none"
          >
        </BookCover>

        <BookTitle
          class="book-title"
          :loading="!showBookInfo"
          :book="book"
        />

        <BookButtons
          class="book-buttons pt-1.5"
          :loading="!showBookInfo"
          :book="book"
          :editing="false"
        />

        <div class="book-synopsis flex flex-col gap-4 sm:gap-6">
          <BlockMarkdown
            :title="$t('common-fields.synopsis')"
            :empty-message="$t('books.empty-synopsis')"
            :loading="!showBookInfo"
            :markdown="book?.attributes?.synopsis ?? undefined"
            :blur="false"
          />

          <BookContributors
            :loading="isLoadingContributors"
            :contributors="contributors"
          />
        </div>

        <div class="book-attributes">
          <BookAttributes
            class="sticky top-24"
            :loading="!showBookInfo"
            :book="book"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<route lang="yaml">
meta:
  layout: dashboard
  transparentNavbar: true
</route>

<style lang="postcss">
.book-grid {
  display: grid;
  gap: 1rem;
  grid-template-areas:
    'art title'
    'buttons buttons'
    'synopsis synopsis'
    'attributes attributes'
    'notes notes'
    'tabs tabs';
  grid-template-columns: 6rem 1fr;

  @media (min-width: theme('screens.sm')) {
    gap: 1.5rem;
    grid-template-areas:
      'art title'
      'art buttons'
      'art padding'
      'attributes synopsis'
      'attributes tags'
      'attributes notes';
    grid-template-columns: 14rem 1fr;
  }

  @media (min-width: theme('screens.2xl')) {
    gap: 1.5rem;
    grid-template-areas:
      'art title title'
      'art buttons buttons'
      'art padding padding'
      'attributes synopsis right'
      'attributes tags right'
      'attributes notes right';
    grid-template-columns: 14rem 1fr 22rem;
  }

  .book-cover {
    grid-area: art / art / art / art;
  }

  .book-buttons {
    grid-area: buttons / buttons / buttons / buttons;
  }

  .book-title {
    grid-area: title / title / title / title;
  }

  .book-synopsis {
    grid-area: synopsis / synopsis / synopsis / synopsis;
  }

  .book-attributes {
    grid-area: attributes / attributes / attributes / attributes;
  }

  .book-right {
    grid-area: right / right / right / right;
  }
}
</style>
