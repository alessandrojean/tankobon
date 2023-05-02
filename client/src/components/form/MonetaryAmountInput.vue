<script lang="ts" setup>
import InputMask from 'inputmask'
import type { ErrorObject } from '@vuelidate/core'
import type { MonetaryAmountString } from '@/types/tankobon-monetary'

export interface DimensionsInputProps {
  errorsAmount?: ErrorObject[]
  errorsCurrency?: ErrorObject[]
  invalidAmount?: boolean
  invalidCurrency?: boolean
  labelText: string
  modelValue: MonetaryAmountString
  placeholderAmount?: string
  placeholderCurrency?: string
  required?: boolean
}

const props = withDefaults(defineProps<DimensionsInputProps>(), {
  errorsAmount: undefined,
  errorsCurrency: undefined,
  invalidAmount: false,
  invalidCurrency: false,
  placeholderAmount: undefined,
  placeholderCurrency: undefined,
  required: false,
})

const emit = defineEmits<{
  (e: 'blur:amount', event: Event): void
  (e: 'blur:currency', event: Event): void
  (e: 'focus:amount', event: Event): void
  (e: 'focus:currency', event: Event): void
  (e: 'update:modelValue', modelValue: MonetaryAmountString): void
}>()

const { errorsAmount, errorsCurrency, modelValue } = toRefs(props)

const errorMessage = computed(() => {
  const errorAmount = unref(errorsAmount.value?.[0]?.$message)
  const errorCurrency = unref(errorsCurrency.value?.[0]?.$message)

  if (errorAmount === errorCurrency)
    return errorAmount
  else if (errorAmount && errorAmount.length > 0)
    return errorAmount
  else if (errorCurrency && errorCurrency.length > 0)
    return errorCurrency
  else
    return null
})

type Focused = 'amount' | 'currency'
const focused = ref<Focused | null>(null)

const focusedLabelText: Record<Focused, string> = {
  amount: 'common-fields.amount',
  currency: 'common-fields.currency',
}

const currencyInput = ref<HTMLInputElement>()
const amountInput = ref<HTMLInputElement>()
const inputMaskAmount = ref<InputMask.Instance>()
const inputMask: InputMask.Options = {
  regex: '\\d+([,.]\\d{1,2})?',
  placeholder: ' ',
  showMaskOnHover: false,
  showMaskOnFocus: false,
}

watchEffect((onCleanup) => {
  if (amountInput.value) {
    inputMaskAmount.value = InputMask(inputMask).mask(amountInput.value)

    onCleanup(() => {
      inputMaskAmount.value?.remove()
    })
  }
  else {
    inputMaskAmount.value?.remove()
  }
})

interface EventHandler { event: Event; type: Focused }

function handleInput({ event, type }: EventHandler) {
  const newDimensions: MonetaryAmountString = { ...modelValue.value }
  const input = event.target as HTMLInputElement

  if (type === 'currency')
    newDimensions.currency = input.value
  else
    newDimensions.amount = input.value

  emit('update:modelValue', newDimensions)
}

function handleBlur({ event, type }: EventHandler) {
  focused.value = null

  if (type === 'currency')
    emit('blur:currency', event)
  else
    emit('blur:amount', event)
}

function handleFocus({ event, type }: EventHandler) {
  focused.value = type

  if (type === 'currency')
    emit('focus:currency', event)
  else
    emit('focus:amount', event)
}

// TODO: Show all currencies.
const DEFAULT_CURRENCIES = ['USD', 'EUR', 'GBP', 'CAD', 'BRL', 'JPY']
const { locale } = useI18n()

const currencyNames = computed(() => new Intl.DisplayNames([locale.value], {
  type: 'currency',
}))

function getCurrencyName(currency: string) {
  try {
    const currencyName = currencyNames.value.of(currency) ?? null

    return currencyName === currency ? null : currencyName
  }
  catch (_) {
    return null
  }
}

const query = ref('')

const queryCurrency = computed(() => {
  return query.value === '' ? null : query.value
})

const filteredCurrencies = computed(() => {
  let filtered = DEFAULT_CURRENCIES
    .map<[string, string | null]>(cc => [cc, getCurrencyName(cc)])
    .filter(([cc, name]) => {
      return cc.toLowerCase().includes(query.value.toLowerCase())
        || query.value.toLowerCase().includes(name ?? '')
    })
    .map(([cc]) => cc)
    .sort((a, b) => a.localeCompare(b, locale.value))

  if (filtered.length === 0 && queryCurrency.value && queryCurrency.value.length === 3)
    filtered = [queryCurrency.value]

  return filtered
})

function handleCurrency(newCurrency: string) {
  const newDimensions: MonetaryAmountString = {
    ...modelValue.value,
    currency: newCurrency,
  }

  emit('update:modelValue', newDimensions)
}
</script>

<template>
  <div class="w-full">
    <fieldset
      class="min-w-0 block border bg-white dark:bg-gray-950 shadow-sm rounded-md border-gray-300 dark:border-gray-700"
    >
      <label
        class="font-medium text-xs px-3 pt-3 select-none cursor-text block text-gray-700 dark:text-gray-300"
        :for="($attrs.id as string | undefined)"
      >
        {{ focused ? $t(focusedLabelText[focused]) : labelText }}
      </label>

      <div class="flex flex-row-reverse gap-2 items-center w-full px-3 pb-2 pt-1">
        <input
          ref="amountInput"
          class="grow min-w-0 bg-white dark:bg-gray-950 rounded-md dark:text-gray-200 border-0 focus:outline-none focus:ring  motion-safe:transition-shadow placeholder:text-gray-500 h-6 px-1.5 text-right motion-safe:transition tabular-nums" :class="[
            invalidAmount
              ? 'ring-1 focus:ring-1 ring-red-500 focus:ring-red-500 dark:ring-red-500/95 dark:focus:ring-red-500/95'
              : 'hover:ring-1 hover:ring-gray-300 dark:hover:ring-gray-700 focus:ring-1 focus:ring-primary-500 dark:focus:ring-primary-500',
          ]"
          type="text"
          inputmode="decimal"
          :required="required"
          :value="modelValue.amount"
          :placeholder="placeholderCurrency"
          @input="handleInput({ event: $event, type: 'amount' })"
          @focus="handleFocus({ event: $event, type: 'amount' })"
          @blur="handleBlur({ event: $event, type: 'amount' })"
        >
        <Combobox
          as="div"
          class="relative"
          :model-value="modelValue.currency"
          @update:model-value="handleCurrency"
        >
          <ComboboxInput
            ref="currencyInput"
            class="w-12 shrink-0 bg-white dark:bg-gray-950 rounded-md dark:text-gray-200 border-0 focus:outline-none focus:ring motion-safe:transition-shadow placeholder:text-gray-500 h-6 px-1.5 motion-safe:transition tabular-nums [font-feature-settings:'cv08']" :class="[
              invalidCurrency
                ? 'ring-1 focus:ring-1 ring-red-500 focus:ring-red-500 dark:ring-red-500/95 dark:focus:ring-red-500/95'
                : 'hover:ring-1 hover:ring-gray-300 dark:hover:ring-gray-700 focus:ring-1 focus:ring-primary-500 dark:focus:ring-primary-500',
            ]"
            type="text"
            inputmode="decimal"
            :required="required"
            :placeholder="placeholderAmount"
            @focus="handleFocus({ event: $event, type: 'currency' })"
            @blur="handleBlur({ event: $event, type: 'currency' })"
            @change="query = $event.target.value"
          />
          <MenuTransition>
            <ComboboxOptions
              v-if="filteredCurrencies.length > 0"
              class="absolute top-full left-0 min-w-[14rem] z-10 p-2 space-y-1 bg-white shadow-primer-overlay dark:shadow-primer-overlay-dark rounded-xl mt-0.5 origin-top-right ring-1 ring-black/5 dark:ring-gray-700"
            >
              <ComboboxOption
                v-for="currencyOption of filteredCurrencies"
                :key="currencyOption"
                :value="currencyOption"
                class="select-none px-2 py-1.5 cursor-pointer text-sm rounded-lg flex gap-1 items-center ui-active:bg-primary-100 ui-active:text-primary-700"
              >
                <span class="block grow">
                  {{ getCurrencyName(currencyOption) ?? $t('monetary-amount-input.unknown-currency') }}
                </span>
                <span
                  class="block shrink-0 font-mono text-xs h-3.5 text-gray-600 ui-active:text-primary-600"
                >
                  {{ currencyOption }}
                </span>
              </ComboboxOption>
            </ComboboxOptions>
          </MenuTransition>
        </Combobox>
      </div>
    </fieldset>

    <slot name="footer" />

    <p
      v-if="(invalidAmount || invalidCurrency) && errorMessage"
      class="text-red-700 dark:text-red-500/95 text-sm font-medium ml-2 mt-1 mb-4"
    >
      {{ errorMessage }}
    </p>
  </div>
</template>
