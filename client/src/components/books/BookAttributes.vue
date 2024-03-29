<script lang="ts" setup>
import {
  ArrowTrendingDownIcon,
  ArrowTrendingUpIcon,
  ExclamationTriangleIcon,
} from '@heroicons/vue/20/solid'
import configureMeasurements, { length, mass } from 'convert-units'
import type { BookEntity } from '@/types/tankobon-book'
import { isIsbnCode } from '@/types/tankobon-book'
import type { MonetaryAmount } from '@/types/tankobon-monetary'
import { getRelationship, getRelationships } from '@/utils/api'
import { isMassUnit, lengthUnitMap, massUnitMap, unitAbbreviation } from '@/utils/unit'
import type { LengthUnit, MassUnit } from '@/types/tankobon-unit'

export interface BookAttributesProps {
  book?: BookEntity | null
  loading?: boolean
}

const props = withDefaults(defineProps<BookAttributesProps>(), {
  book: undefined,
  loading: false,
})

defineEmits<{
  (e: 'click:publisher', publisher: string): void
  (e: 'click:group', group: string): void
  (e: 'click:store', store: string): void
}>()

const { book, loading } = toRefs(props)
const { t, d, n, locale } = useI18n()

const convertMass = configureMeasurements({ mass })
const convertLength = configureMeasurements({ length })

function formatPrice(price: MonetaryAmount | null | undefined) {
  if (!price) {
    return null
  }

  const { amount, currency } = price

  // @ts-expect-error wrong type from lib
  return n(amount, 'currency', { currency })
}

function formatDate(date: string | null | undefined, format = 'dateTime') {
  if (typeof date === 'string' && date.length > 0) {
    return d(
      new Date(date),
      format,
    )
  }

  return t('date.unknown')
}

const isbnCode = computed(() => {
  return isIsbnCode(book.value?.attributes?.code)
    ? book.value?.attributes?.code
    : null
})

const language = computed(() => {
  if (!isbnCode.value || !isbnCode.value.language) {
    return null
  }

  const languageNames = new Intl.DisplayNames([locale.value], {
    type: 'language',
  })
  const localizedName = languageNames.of(isbnCode.value.language)!

  return (
    localizedName.charAt(0).toLocaleUpperCase(locale.value)
    + localizedName.slice(1)
  )
})

const { preference: preferredLengthUnit } = useUserPreference<LengthUnit>('preferred_length_unit', 'CENTIMETER')
const { preference: preferredMassUnit } = useUserPreference<MassUnit>('preferred_mass_unit', 'KILOGRAM')

function convertValue(value: number | undefined, unit: MassUnit | LengthUnit | undefined) {
  if (!value || !unit) {
    return 0
  }

  if (isMassUnit(unit)) {
    return convertMass(value)
      .from(massUnitMap[unit])
      .to(massUnitMap[preferredMassUnit.value])
  }

  return convertLength(value)
    .from(lengthUnitMap[unit])
    .to(lengthUnitMap[preferredLengthUnit.value])
}

const metadata = computed(() => {
  const attributes = book.value?.attributes
  const sameCurrency
    = book.value?.attributes?.paidPrice?.currency === book.value?.attributes?.labelPrice?.currency
  const publishers = getRelationships(book.value, 'PUBLISHER')
  const collection = getRelationship(book.value, 'COLLECTION')
  const store = getRelationship(book.value, 'STORE')
  const series = getRelationship(book.value, 'SERIES')

  const weightValue = convertValue(attributes?.weight?.value, attributes?.weight?.unit)
  const dimensions = {
    width: convertValue(attributes?.dimensions?.width, attributes?.dimensions?.unit),
    height: convertValue(attributes?.dimensions?.height, attributes?.dimensions?.unit),
    depth: convertValue(attributes?.dimensions?.depth, attributes?.dimensions?.unit),
  }

  return [
    {
      title: t('common-fields.language'),
      value: language.value,
    },
    {
      title: (attributes?.code.type ?? 'UNKNOWN') !== 'UNKNOWN'
        ? attributes!.code.type.replace(/_/g, '-')
        : t('common-fields.code'),
      value: attributes?.code.code,
    },
    {
      title: t('common-fields.series'),
      key: 'series',
      value: series?.attributes?.name,
      link: {
        name: 'series-id',
        params: { id: series?.id },
      },
    },
    {
      title: t('publishers.publisher', publishers?.length ?? 0),
      key: 'publishers',
      values: publishers?.map(p => ({
        key: p.id,
        value: p.attributes!.name,
        link: {
          name: 'publishers-id',
          params: { id: p.id },
        },
      })),
    },
    {
      title: t('common-fields.collection'),
      key: 'collection',
      value: collection?.attributes?.name,
      link: {
        name: 'collections-id',
        params: { id: collection?.id },
      },
    },
    {
      title: t('common-fields.dimensions'),
      value: attributes?.dimensions
        ? `${n(dimensions.width, 'dimension')
          } × ${
          n(dimensions.height, 'dimension')
          } × ${
          n(dimensions.depth, 'dimension')
          } ${unitAbbreviation[preferredLengthUnit.value]}`
        : null,
    },
    {
      title: t('common-fields.page-count'),
      value: n(attributes?.pageCount ?? 0, 'integer'),
      warning: attributes?.pageCount === 0,
    },
    {
      title: t('common-fields.weight'),
      value: n(
        weightValue,
        'unit',
        // @ts-expect-error The signature is wrong at the library.
        { unit: preferredMassUnit.value.toLowerCase() },
      ),
    },
    {
      title: t('common-fields.label-price'),
      value: formatPrice(attributes?.labelPrice),
    },
    {
      title: t('common-fields.paid-price'),
      value: formatPrice(attributes?.paidPrice),
      badge: sameCurrency
        ? (attributes?.paidPrice?.amount ?? 0)
          > (attributes?.labelPrice?.amount ?? 0)
            ? (attributes?.paidPrice?.amount ?? 1)
            / (attributes?.labelPrice?.amount ?? 1)
            : 1
            - (attributes?.paidPrice?.amount ?? 1)
              / (attributes?.labelPrice?.amount ?? 1)
        : null,
      samePrice: attributes?.paidPrice?.amount === attributes?.labelPrice?.amount,
    },
    {
      title: t('common-fields.store'),
      key: 'store',
      value: store?.attributes?.name,
      link: {
        name: 'stores-id',
        params: { id: store?.id },
      },
    },
    {
      title: t('common-fields.bought-at'),
      value: attributes?.boughtAt,
      time: true,
    },
    {
      title: t('common-fields.billed-at'),
      value: attributes?.billedAt,
      time: true,
    },
    {
      title: t('common-fields.arrived-at'),
      value: attributes?.arrivedAt,
      time: true,
    },
    // {
    //   title: t('book.properties.readAt'),
    //   value: book.value?.readAt,
    //   time: true
    // }
  ]
})
</script>

<template>
  <Block as="dl">
    <div class="space-y-5">
      <template v-for="(mt, i) in metadata">
        <div v-if="mt.value || mt.values || loading" :key="i">
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
            <time v-if="mt.time" :datetime="mt.value">
              {{ formatDate(mt.value!) }}
            </time>
            <RouterLink
              v-else-if="mt.link"
              :to="mt.link"
              :title="$t('common-actions.go-to-page', [mt.value])"
              class="hover:underline hover:text-primary-600 dark:hover:text-primary-500 motion-safe:transition"
            >
              {{ mt.value }}
            </RouterLink>
            <ul
              v-else-if="mt.values"
              class="space-y-1"
            >
              <li
                v-for="value in mt.values"
                :key="value.key"
              >
                <RouterLink
                  :to="value.link"
                  :title="$t('common-actions.go-to-page', [value.value])"
                  class="hover:underline hover:text-primary-600 dark:hover:text-primary-500 motion-safe:transition"
                >
                  {{ value.value }}
                </RouterLink>
              </li>
            </ul>
            <span v-else>{{ mt.value }}</span>

            <Badge
              v-if="mt.badge && !mt.samePrice"
              class="ml-2"
              rounded
              :color="mt.badge <= 1 ? 'green' : 'red'"
            >
              <ArrowTrendingDownIcon v-if="mt.badge <= 1" class="w-4 h-4" />
              <ArrowTrendingUpIcon v-else class="w-4 h-4" />
              <span>{{ n(mt.badge, 'percent') }}</span>
            </Badge>

            <ExclamationTriangleIcon
              v-if="mt.warning"
              class="w-5 h-5 ml-1 text-amber-500"
            />
          </dd>
          <div v-else aria-hidden="true" class="mt-1 skeleton w-32 h-6" />
        </div>
      </template>
    </div>
  </Block>
</template>
