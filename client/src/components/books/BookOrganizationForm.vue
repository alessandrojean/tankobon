<script lang="ts" setup>
import { useVuelidate } from '@vuelidate/core'
import { helpers, required } from '@vuelidate/validators'
import MonetaryAmountInput from '../form/MonetaryAmountInput.vue'
import type { MonetaryAmountString } from '@/types/tankobon-monetary'
import { convertLocalTimeZoneToUtc, convertUtcToLocalTimeZone } from '@/utils/date'
import { positiveDecimal } from '@/utils/validation'
import type { StoreAttributes, StoreEntity } from '@/types/tankobon-store'
import { createEmptyPaginatedResponse } from '@/utils/api'
import type { TagEntity } from '@/types/tankobon-tag'

export interface BookMetadataFormProps {
  collection: string
  notes: string
  billedAt: string | null | undefined
  boughtAt: string | null | undefined
  arrivedAt: string | null | undefined
  labelPrice: MonetaryAmountString
  paidPrice: MonetaryAmountString
  mode?: 'creation' | 'update'
  store: string | null | undefined
  tags: string[] | null | undefined
  disabled?: boolean
}

export interface BookMetadataFormEmits {
  (e: 'update:collection', collection: string): void
  (e: 'update:notes', notes: string): void
  (e: 'update:billedAt', billedAt: string | null): void
  (e: 'update:arrivedAt', arrivedAt: string | null): void
  (e: 'update:boughtAt', boughtAt: string | null): void
  (e: 'update:labelPrice', labelPrice: MonetaryAmountString): void
  (e: 'update:paidPrice', paidPrice: MonetaryAmountString): void
  (e: 'update:store', store: string | null): void
  (e: 'update:tags', tags: string[] | null | undefined): void
  (e: 'validate', isValid: boolean): void
}

const props = withDefaults(defineProps<BookMetadataFormProps>(), {
  disabled: false,
  mode: 'creation',
})
const emit = defineEmits<BookMetadataFormEmits>()

const { paidPrice, labelPrice, store, collection, tags } = toRefs(props)

const { t } = useI18n()
const notificator = useToaster()
const libraryStore = useLibraryStore()
const libraryId = computed(() => libraryStore.library!.id)

const rules = computed(() => {
  const messageRequired = helpers.withMessage(t('validation.required'), required)
  const messageDecimal = helpers.withMessage(t('validation.decimal'), positiveDecimal)

  return {
    collection: { messageRequired },
    labelPrice: {
      amount: { messageRequired, messageDecimal },
      currency: { messageRequired },
    },
    paidPrice: {
      amount: { messageRequired, messageDecimal },
      currency: { messageRequired },
    },
  }
})

const v$ = useVuelidate(rules, { paidPrice, labelPrice, collection })

watch(() => v$.value.$error, isValid => emit('validate', isValid))

defineExpose({ v$ })

function handleDateTimeInput(event: KeyboardEvent, field: 'boughtAt' | 'billedAt' | 'arrivedAt') {
  const input = event.target as HTMLInputElement
  const value = input.value.length > 0
    ? convertLocalTimeZoneToUtc(input.value)
    : null

  if (field === 'boughtAt') {
    emit('update:boughtAt', value)
  } else if (field === 'billedAt') {
    emit('update:billedAt', value)
  } else {
    emit('update:arrivedAt', value)
  }
}

const { data: stores } = useLibraryStoresQuery({
  libraryId,
  sort: [{ property: 'name', direction: 'asc' }],
  unpaged: true,
  select: response => response.data,
  initialData: () => createEmptyPaginatedResponse(),
  onError: async (error) => {
    await notificator.failure({
      title: t('stores.fetch-failure'),
      body: error.message,
    })
  },
})

const { data: collections } = useLibraryCollectionsQuery({
  libraryId,
  sort: [{ property: 'name', direction: 'asc' }],
  unpaged: true,
  select: response => response.data,
  initialData: () => createEmptyPaginatedResponse(),
  onError: async (error) => {
    await notificator.failure({
      title: t('collections.fetch-failure'),
      body: error.message,
    })
  },
})

const { data: libraryTags } = useLibraryTagsQuery({
  libraryId,
  sort: [{ property: 'name', direction: 'asc' }],
  unpaged: true,
  select: response => response.data,
  initialData: () => createEmptyPaginatedResponse(),
  onError: async (error) => {
    await notificator.failure({
      title: t('tags.fetch-failure'),
      body: error.message,
    })
  },
})

const nullStore = computed<StoreEntity>(() => ({
  type: 'STORE',
  id: 'null',
  attributes: { name: t('stores.unknown') } as StoreAttributes,
  relationships: [],
}))

const storeValue = computed(() => {
  return stores.value!.find(s => s.id === store.value) ?? nullStore.value
})

const collectionValue = computed(() => {
  return collections.value!.find(c => c.id === collection.value)
})

const storeOptions = computed(() => {
  return [nullStore.value, ...stores.value!]
})

const tagMap = computed(() => {
  return Object.fromEntries(
    libraryTags.value!.map(p => [p.id, p]),
  )
})
</script>

<template>
  <fieldset class="space-y-6" :disabled="disabled">
    <fieldset class="grid grid-cols-1 lg:grid-cols-3 gap-2">
      <SearchableCombobox
        id="collection"
        kind="fancy"
        :placeholder="$t('common-placeholders.book-collection')"
        :label-text="$t('common-fields.collection')"
        :model-value="collectionValue"
        :options="collections ?? []"
        :option-text="r => r?.attributes?.name ?? ''"
        :option-value="r => r"
        :option-value-select="r => r?.id"
        :invalid="v$.collection.$error"
        :errors="v$.collection.$errors"
        @blur="v$.collection.$touch()"
        @update:model-value="$emit('update:collection', $event?.id)"
        @update:model-value-select="$emit('update:collection', $event)"
      />
    </fieldset>

    <fieldset class="flex flex-col gap-2">
      <div class="grid grid-cols-1 lg:grid-cols-4 xl:grid-cols-12 gap-2">
        <MonetaryAmountInput
          id="label-price"
          class="xl:col-span-2"
          :placeholder-amount="$t('common-placeholders.book-label-price-amount')"
          :placeholder-currency="$t('common-placeholders.book-label-price-currency')"
          :model-value="labelPrice"
          :label-text="$t('common-fields.label-price')"
          :invalid-amount="v$.labelPrice.amount.$error"
          :invalid-currency="v$.labelPrice.currency.$error"
          :errors-amount="v$.labelPrice.amount.$errors"
          :errors-currency="v$.labelPrice.currency.$errors"
          @blur:amount="v$.labelPrice.amount.$touch()"
          @blur:currency="v$.labelPrice.currency.$touch()"
          @update:model-value="$emit('update:labelPrice', $event)"
        />
        <MonetaryAmountInput
          id="paid-price"
          class="xl:col-span-2"
          :placeholder-amount="$t('common-placeholders.book-paid-price-amount')"
          :placeholder-currency="$t('common-placeholders.book-paid-price-currency')"
          :model-value="paidPrice"
          :label-text="$t('common-fields.paid-price')"
          :invalid-amount="v$.paidPrice.amount.$error"
          :invalid-currency="v$.paidPrice.currency.$error"
          :errors-amount="v$.paidPrice.amount.$errors"
          :errors-currency="v$.paidPrice.currency.$errors"
          @blur:amount="v$.paidPrice.amount.$touch()"
          @blur:currency="v$.paidPrice.currency.$touch()"
          @update:model-value="$emit('update:paidPrice', $event)"
        />
        <SearchableCombobox
          id="store"
          kind="fancy"
          class="lg:col-span-2 xl:col-span-4"
          :placeholder="$t('common-placeholders.book-store')"
          :label-text="$t('common-fields.store')"
          :model-value="storeValue"
          :options="storeOptions ?? []"
          :option-text="r => r?.attributes?.name ?? ''"
          :option-value="r => r"
          :option-value-select="r => r?.id ?? 'null'"
          @update:model-value="$emit('update:store', $event?.id === 'null' ? null : $event?.id)"
          @update:model-value-select="$emit('update:store', $event === 'null' ? null : $event)"
        />
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 xl:grid-cols-8 gap-2">
        <div class="xl:col-span-2">
          <TextInput
            id="bought-at"
            :model-value="boughtAt ? convertUtcToLocalTimeZone(boughtAt) : ''"
            type="datetime-local"
            :label-text="$t('common-fields.bought-at')"
            @input="handleDateTimeInput($event, 'boughtAt')"
          />
        </div>
        <div class="xl:col-span-2">
          <TextInput
            id="billed-at"
            :model-value="billedAt ? convertUtcToLocalTimeZone(billedAt) : ''"
            type="datetime-local"
            :label-text="$t('common-fields.billed-at')"
            @input="handleDateTimeInput($event, 'billedAt')"
          />
        </div>
        <div class="xl:col-span-2">
          <TextInput
            id="arrived-at"
            :model-value="arrivedAt ? convertUtcToLocalTimeZone(arrivedAt) : ''"
            type="datetime-local"
            :label-text="$t('common-fields.arrived-at')"
            @input="handleDateTimeInput($event, 'arrivedAt')"
          />
        </div>
      </div>
    </fieldset>

    <ChipInput
      :placeholder="$t('common-placeholders.book-tag')"
      :label-text="$t('entities.tags')"
      :model-value="tags ?? []"
      :model-text="(p: string) => tagMap[p]?.attributes.name"
      :options="libraryTags ?? []"
      :option-text="(p: TagEntity) => p.attributes?.name"
      :option-value="(p: TagEntity) => p.id"
      @update:model-value="$emit('update:tags', $event)"
    />

    <MarkdownInput
      id="notes"
      :model-value="notes ?? ''"
      rows="5"
      :label-text="$t('common-fields.notes')"
      :placeholder="$t('common-placeholders.book-notes')"
      @input="$emit('update:notes', $event.target.value)"
    />
  </fieldset>
</template>
