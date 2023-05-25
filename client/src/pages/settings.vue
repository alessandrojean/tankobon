<script setup lang="ts">
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import { countries as regions } from 'countries-list'
import { getLanguageName, getRegionCode } from '@/utils/language'
import type { LengthUnit, MassUnit } from '@/types/tankobon-unit'
import { isMassUnit, unitAbbreviation } from '@/utils/unit'
import type { Theme } from '@/App.vue'

const { t, locale, availableLocales } = useI18n()
const router = useRouter()

useHead({ title: () => t('settings.title') })

function getLocaleName(locale: string) {
  return getLanguageName({
    language: locale,
    locale,
    romanizedLabel: '',
    unknownLabel: '',
  })
}

const { preference: preferredCurrency } = useUserPreference<string>('preferred_currency', 'USD')
const { preference: preferredLengthUnit } = useUserPreference<LengthUnit>('preferred_length_unit', 'CENTIMETER')
const { preference: preferredMassUnit } = useUserPreference<MassUnit>('preferred_mass_unit', 'KILOGRAM')
const { preference: theme } = useUserPreference<Theme>('theme', 'system')
const localTheme = useLocalStorage<Theme>('theme', 'system')

watch(theme, newTheme => localTheme.value = newTheme)

function getUnitName(unit: LengthUnit | MassUnit) {
  const type = isMassUnit(unit) ? 'mass' : 'length'
  const key = unit.toLowerCase().replace(/_/g, '-')

  return t(`units.${type}.${key}`)
}

const currencyNames = computed(() => new Intl.DisplayNames([locale.value], {
  type: 'currency',
}))

function getCurrencyName(currency: string) {
  try {
    return currencyNames.value.of(currency) ?? t('monetary-amount-input.unknown-currency')
  } catch (_) {
    return t('monetary-amount-input.unknown-currency')
  }
}

const currencies = computed(() => {
  return [...new Set(Object.values(regions).flatMap(r => r.currency.split(',')))]
    .map(code => ({ code, name: getCurrencyName(code) }))
    .filter(({ name }) => name !== null)
    .sort((a, b) => a.name!.localeCompare(b.name!, locale.value))
})
</script>

<route lang="yaml">
meta:
  layout: dashboard
</route>

<template>
  <div>
    <Header class="mb-3 md:mb-0" :title="$t('settings.title')">
      <template #avatar>
        <Button
          class="aspect-1 w-10 h-10 -ml-2"
          size="mini"
          kind="ghost"
          rounded="full"
          :title="$t('common-actions.back')"
          @click="router.back"
        >
          <span class="sr-only">
            {{ $t('common-actions.back') }}
          </span>
          <ArrowLeftIcon class="w-5 h-5" />
        </Button>
      </template>
    </Header>

    <div class="max-w-7xl mx-auto p-4 sm:p-6 @container">
      <Setting
        :title="$t('settings.language-title')"
        :description="$t('settings.language-description')"
      >
        <BasicListbox
          v-model="locale"
          class="w-56"
          :label-text="$t('settings.language-title')"
          :options="availableLocales"
          :option-text="(locale) => getLocaleName(locale!)"
          :option-value="(locale) => locale"
        >
          <template #button="{ selected, text }">
            <div class="flex items-center gap-3 w-full min-w-0">
              <Flag
                class="shrink-0"
                :region="getRegionCode(selected.option)"
              />
              <span class="block truncate">
                {{ text }}
              </span>
            </div>
          </template>
          <template #option="{ option }">
            <div :lang="option" class="flex items-center gap-3 w-full min-w-0">
              <div class="grow truncate">
                {{ getLocaleName(option) }}
              </div>
              <Flag
                class="shrink-0"
                :region="getRegionCode(option)"
              />
            </div>
          </template>
        </BasicListbox>
      </Setting>

      <Setting
        :title="$t('settings.theme-title')"
        :description="$t('settings.theme-description')"
      >
        <BasicListbox
          v-model="theme"
          class="w-56"
          :label-text="$t('settings.theme-title')"
          :options="(['light', 'dark', 'system'] as Theme[])"
          :option-text="(theme) => $t(`theme-toggle.${theme}`)"
          :option-value="(theme) => theme"
        />
      </Setting>

      <Setting
        :title="$t('settings.preferred-currency-title')"
        :description="$t('settings.preferred-currency-description')"
      >
        <BasicListbox
          v-model="preferredCurrency"
          class="w-56"
          :label-text="$t('settings.preferred-currency-title')"
          :options="currencies"
          :option-text="(currency) => currency!.name"
          :option-value="(currency) => currency.code"
        >
          <template #button="{ text, selected }">
            <span class="block truncate">
              {{ `${text} (${selected.option.code})` }}
            </span>
          </template>
          <template #option="{ text, option }">
            <div class="flex gap-6 items-center">
              <span
                :class="[
                  'block truncate grow',
                  'text-black dark:text-gray-300 ui-active:text-primary-700',
                  'dark:ui-active:dark:text-primary-100',
                ]"
              >
                {{ text }}
              </span>
              <span
                :class="[
                  'block shrink-0 text-xs font-mono h-3.5',
                  'text-gray-600 ui-active:text-primary-600',
                  'dark:text-gray-500 dark:ui-active:text-primary-200',
                ]"
              >
                {{ option.code }}
              </span>
            </div>
          </template>
        </BasicListbox>
      </Setting>

      <Setting
        :title="$t('settings.preferred-length-unit-title')"
        :description="$t('settings.preferred-length-unit-description')"
      >
        <BasicListbox
          v-model="preferredLengthUnit"
          class="w-56"
          :label-text="$t('settings.preferred-length-unit-title')"
          :options="(['CENTIMETER', 'INCH', 'MILLIMETER'] as LengthUnit[])"
          :option-text="(unit) => getUnitName(unit!)"
          :option-value="(unit) => unit"
        >
          <template #button="{ text, selected }">
            <span class="block truncate">
              {{ `${text} (${unitAbbreviation[selected.option]})` }}
            </span>
          </template>
          <template #option="{ text, option }">
            <div class="flex gap-6 items-center">
              <span
                :class="[
                  'block truncate grow',
                  'text-black dark:text-gray-300 ui-active:text-primary-700',
                  'dark:ui-active:dark:text-primary-100',
                ]"
              >
                {{ text }}
              </span>
              <span
                :class="[
                  'block shrink-0 text-xs',
                  'text-gray-600 ui-active:text-primary-600',
                  'dark:text-gray-500 dark:ui-active:text-primary-200',
                ]"
              >
                {{ unitAbbreviation[option] }}
              </span>
            </div>
          </template>
        </BasicListbox>
      </Setting>

      <Setting
        :title="$t('settings.preferred-mass-unit-title')"
        :description="$t('settings.preferred-mass-unit-description')"
      >
        <BasicListbox
          v-model="preferredMassUnit"
          class="w-56"
          :label-text="$t('settings.preferred-length-unit-title')"
          :options="(['KILOGRAM', 'GRAM', 'OUNCE', 'POUND'] as MassUnit[])"
          :option-text="(unit) => getUnitName(unit!)"
          :option-value="(unit) => unit"
        >
          <template #button="{ text, selected }">
            <span class="block truncate">
              {{ `${text} (${unitAbbreviation[selected.option]})` }}
            </span>
          </template>
          <template #option="{ text, option }">
            <div class="flex gap-6 items-center">
              <span
                :class="[
                  'block truncate grow',
                  'text-black dark:text-gray-300 ui-active:text-primary-700',
                  'dark:ui-active:dark:text-primary-100',
                ]"
              >
                {{ text }}
              </span>
              <span
                :class="[
                  'block shrink-0 text-xs',
                  'text-gray-600 ui-active:text-primary-600',
                  'dark:text-gray-500 dark:ui-active:text-primary-200',
                ]"
              >
                {{ unitAbbreviation[option] }}
              </span>
            </div>
          </template>
        </BasicListbox>
      </Setting>
    </div>
  </div>
</template>
