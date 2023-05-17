<script lang="ts" setup>
import { useVuelidate } from '@vuelidate/core'
import { helpers, integer, minValue, required } from '@vuelidate/validators'
import { BuildingOffice2Icon, XMarkIcon } from '@heroicons/vue/20/solid'
import type { DimensionsString } from '@/types/tankobon-dimensions'
import { positiveDecimal } from '@/utils/validation'
import { createEmptyPaginatedResponse, getRelationship } from '@/utils/api'
import type { SeriesEntity } from '@/types/tankobon-series'
import type { PublisherEntity } from '@/types/tankobon-publisher'
import { createImageUrl } from '@/modules/api'

export interface BookMetadataFormProps {
  disabled?: boolean
  code: string
  barcode: string | null | undefined
  number: string
  title: string
  subtitle: string
  synopsis: string
  pageCount: string
  dimensions: DimensionsString
  weight: string
  publishers: string[]
  series: string | null | undefined
  mode?: 'creation' | 'update'
}

export interface BookMetadataFormEmits {
  (e: 'update:code', code: string): void
  (e: 'update:barcode', barcode: string): void
  (e: 'update:number', number: string): void
  (e: 'update:title', title: string): void
  (e: 'update:subtitle', subtitle: string): void
  (e: 'update:synopsis', synopsis: string): void
  (e: 'update:pageCount', pageCount: string): void
  (e: 'update:dimensions', dimensions: DimensionsString): void
  (e: 'update:weight', weight: string): void
  (e: 'update:series', series: string | null): void
  (e: 'update:publishers', publishers: string[]): void
  (e: 'validate', isValid: boolean): void
}

const props = withDefaults(defineProps<BookMetadataFormProps>(), {
  disabled: false,
  mode: 'creation',
})
const emit = defineEmits<BookMetadataFormEmits>()

const { code, title, number, pageCount, dimensions, series, publishers, weight } = toRefs(props)

const { t } = useI18n()

const rules = computed(() => {
  const messageRequired = helpers.withMessage(t('validation.required'), required)
  const messageNotEmpty = helpers.withMessage(t('validation.not-empty'), required)
  const messageInteger = helpers.withMessage(t('validation.integer'), integer)
  const messageMinValue = helpers.withMessage(({ $params }) => t('validation.min-value', [$params.min]), minValue(0))
  const messageDecimal = helpers.withMessage(t('validation.decimal'), positiveDecimal)

  return {
    code: { messageRequired },
    title: { messageRequired },
    pageCount: { messageInteger, messageMinValue },
    weight: { messageRequired },
    publishers: { messageNotEmpty },
    dimensions: {
      widthCm: { messageDecimal },
      heightCm: { messageDecimal },
    },
  }
})

const v$ = useVuelidate(rules, { code, title, pageCount, dimensions, publishers, weight })

watch(() => v$.value.$error, isValid => emit('validate', isValid))
watch(publishers, () => v$.value.publishers.$touch())

defineExpose({ v$ })

const notificator = useToaster()
const libraryStore = useLibraryStore()
const libraryId = computed(() => libraryStore.library!.id)

const { data: librarySeries } = useLibrarySeriesQuery({
  libraryId,
  sort: [{ property: 'name', direction: 'asc' }],
  unpaged: true,
  select: response => response.data,
  initialData: () => createEmptyPaginatedResponse(),
  onError: async (error) => {
    await notificator.failure({
      title: t('series.fetch-failure'),
      body: error.message,
    })
  },
})

const { data: libraryPublishers, isLoading: isLoadingPublishers } = useLibraryPublishersQuery({
  libraryId,
  sort: [{ property: 'name', direction: 'asc' }],
  includes: ['publisher_picture'],
  unpaged: true,
  select: response => response.data,
  initialData: () => createEmptyPaginatedResponse(),
  onError: async (error) => {
    await notificator.failure({
      title: t('publishers.fetch-failure'),
      body: error.message,
    })
  },
})

const nullSeries = computed<SeriesEntity>(() => ({
  type: 'SERIES',
  id: 'null',
  attributes: {
    name: t('series.none'),
    description: '',
    originalLanguage: '',
    type: null,
    lastNumber: '',
    alternativeNames: [],
  },
  relationships: [],
}))

const publisherMap = computed(() => {
  return Object.fromEntries(
    libraryPublishers.value!.map(p => [p.id, p]),
  )
})

const seriesValue = computed(() => {
  return librarySeries.value!.find(s => s.id === series.value) ?? nullSeries.value
})

const seriesOptions = computed(() => {
  return [nullSeries.value, ...librarySeries.value!]
})

function getPublisherPicture(publisher: PublisherEntity) {
  return getRelationship(publisher, 'PUBLISHER_PICTURE')?.attributes
}
</script>

<template>
  <fieldset class="space-y-6" :disabled="disabled">
    <div class="space-y-2">
      <TextInput
        id="title"
        :model-value="title ?? ''"
        required
        :label-text="$t('common-fields.title')"
        :placeholder="$t('common-placeholders.book-title')"
        :invalid="v$.title.$error"
        :errors="v$.title.$errors"
        @blur="v$.title.$touch()"
        @input="$emit('update:title', $event.target.value)"
      />
      <div class="grid grid-cols-1 lg:grid-cols-4 xl:grid-cols-5 gap-2">
        <div class="lg:col-span-3 xl:col-span-4">
          <TextInput
            id="subtitle"
            :model-value="subtitle ?? ''"
            :label-text="$t('common-fields.subtitle')"
            :placeholder="$t('common-placeholders.book-subtitle')"
            @input="$emit('update:subtitle', $event.target.value)"
          />
        </div>
        <TextInput
          id="number"
          :model-value="number ?? ''"
          required
          inputmode="decimal"
          :label-text="$t('common-fields.number')"
          :placeholder="$t('common-placeholders.book-number')"
          @input="$emit('update:number', $event.target.value)"
        />
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-4 gap-2">
      <TextInput
        id="code"
        :model-value="code ?? ''"
        required
        :label-text="$t('common-fields.code')"
        :placeholder="$t('common-placeholders.book-code')"
        :invalid="v$.code.$error"
        :errors="v$.code.$errors"
        @blur="v$.code.$touch()"
        @input="$emit('update:code', $event.target.value)"
      />

      <TextInput
        id="barcode"
        :model-value="barcode ?? ''"
        required
        inputmode="numeric"
        :label-text="$t('common-fields.barcode')"
        :placeholder="$t('common-placeholders.book-barcode')"
        @input="$emit('update:barcode', $event.target.value)"
      />

      <SearchableCombobox
        kind="fancy"
        class="lg:col-span-2"
        :placeholder="$t('common-placeholders.book-series')"
        :label-text="$t('common-fields.series')"
        :model-value="seriesValue"
        :options="seriesOptions ?? []"
        :option-text="(r: SeriesEntity) => r?.attributes?.name"
        :option-value="(r: SeriesEntity) => r"
        :option-value-select="(r: SeriesEntity) => r?.id"
        @update:model-value="$emit('update:series', $event?.id === 'null' ? null : $event?.id)"
        @update:model-value-select="$emit('update:series', $event === 'null' ? null : $event)"
      />
    </div>

    <ChipInput
      :placeholder="$t('common-placeholders.book-publisher')"
      :label-text="$t('entities.publishers')"
      :model-value="publishers"
      :model-text="(p: string) => publisherMap[p]?.attributes.name"
      :options="libraryPublishers ?? []"
      :option-text="(p: PublisherEntity) => p.attributes?.name"
      :option-value="(p: PublisherEntity) => p.id"
      :invalid="v$.publishers.$error"
      :errors="v$.publishers.$errors"
      @blur="v$.publishers.$touch()"
      @update:model-value="$emit('update:publishers', $event)"
    >
      <template #chip="{ option, remove }: { option: string, remove: () => void }">
        <li
          v-if="isLoadingPublishers || !publisherMap[option]"
          class="skeleton w-32 h-7 rounded-lg"
        />
        <li
          v-else
          :class="[
            'flex items-center gap-2 select-none',
            'pl-1 pr-1.5 py-0.5 rounded-lg text-sm',
            'bg-primary-100 text-primary-700',
            'dark:bg-gray-800 dark:text-gray-300',
          ]"
        >
          <Avatar
            square
            size="xxs"
            :empty-icon="BuildingOffice2Icon"
            :picture-url="
              createImageUrl({
                fileName: getPublisherPicture(publisherMap[option])?.versions['64'],
                timeHex: getPublisherPicture(publisherMap[option])?.timeHex,
              })
            "
          />
          <span>
            {{ publisherMap[option]?.attributes.name }}
          </span>
          <Button
            class="w-6 h-6 -mr-1.5"
            size="small"
            kind="ghost-alt"
            rounded="full"
            tabindex="-1"
            :title="$t('common-actions.remove')"
            @click="remove"
          >
            <span class="sr-only">{{ $t('common-actions.remove') }}</span>
            <XMarkIcon class="w-4 h-4 light:text-primary-600 light:group-hover/button:text-primary-700" />
          </Button>
        </li>
      </template>

      <template #option="{ option }: { option: PublisherEntity }">
        <div class="flex items-center gap-3 w-full">
          <Avatar
            square
            class="-ml-0.5 shrink-0"
            size="xs"
            :empty-icon="BuildingOffice2Icon"
            :picture-url="
              createImageUrl({
                fileName: getPublisherPicture(option)?.versions['128'],
                timeHex: getPublisherPicture(option)?.timeHex,
              })
            "
          />
          <div class="grow">
            {{ option?.attributes.name }}
          </div>
        </div>
      </template>
    </ChipInput>

    <MarkdownInput
      id="synopsis"
      :model-value="synopsis ?? ''"
      rows="10"
      :label-text="$t('common-fields.synopsis')"
      :placeholder="$t('common-placeholders.book-synopsis')"
      @input="$emit('update:synopsis', $event.target.value)"
    />

    <div class="grid grid-cols-1 lg:grid-cols-10 gap-2">
      <div class="lg:col-span-2">
        <TextInput
          id="page-count"
          :model-value="pageCount ?? ''"
          required
          inputmode="numeric"
          :input-mask="{
            regex: '\\d+',
            showMaskOnHover: false,
            showMaskOnFocus: false,
          }"
          :placeholder="$t('common-placeholders.book-page-count')"
          :label-text="$t('common-fields.page-count')"
          :invalid="v$.pageCount.$error"
          :errors="v$.pageCount.$errors"
          @blur="v$.pageCount.$touch()"
          @input="$emit('update:pageCount', $event.target.value)"
        />
      </div>

      <DimensionsInput
        id="dimensions"
        class="lg:col-span-3 xl:col-span-2"
        :model-value="dimensions"
        required
        :placeholder-width="$t('common-placeholders.book-width-cm')"
        :placeholder-height="$t('common-placeholders.book-height-cm')"
        :invalid-width="v$.dimensions.widthCm.$error"
        :errors-width="v$.dimensions.widthCm.$errors"
        :invalid-height="v$.dimensions.heightCm.$error"
        :errors-height="v$.dimensions.heightCm.$errors"
        @blur:width="v$.dimensions.widthCm.$touch()"
        @blur:height="v$.dimensions.heightCm.$touch()"
        @update:model-value="$emit('update:dimensions', $event)"
      />

      <div class="lg:col-span-2">
        <TextInput
          id="weight"
          :model-value="weight ?? ''"
          required
          inputmode="numeric"
          unit="kg"
          :input-mask="{
            regex: '\\d+([,.]\\d{1,3})?',
            showMaskOnHover: false,
            showMaskOnFocus: false,
          }"
          :placeholder="$t('common-placeholders.book-weight')"
          :label-text="$t('common-fields.weight')"
          :invalid="v$.weight.$error"
          :errors="v$.weight.$errors"
          @blur="v$.weight.$touch()"
          @input="$emit('update:weight', $event.target.value)"
        />
      </div>
    </div>
  </fieldset>
</template>
