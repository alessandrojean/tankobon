<script lang="ts" setup>
export interface PasswordRequirementsProps {
  minimumLength: number,
  password: string,
}

const props = defineProps<PasswordRequirementsProps>()
const { minimumLength, password } = toRefs(props)

const hasUppercase = computed(() => password.value.match(/[A-Z]/))
const hasLowercase = computed(() => password.value.match(/[a-z]/))
const hasNumber = computed(() => password.value.match(/[0-9]/))
const hasSpecial = computed(() => password.value.match(/[#?!@$%^&*-]/))
const hasMinLength = computed(() => password.value.length >= minimumLength.value)

const criteriumMeetCount = computed(() => {
  const criteriums = [
    hasUppercase.value,
    hasLowercase.value,
    hasNumber.value,
    hasSpecial.value,
  ]

  const content = criteriums.filter(Boolean).length
  const length = hasMinLength.value ? 1 : 0

  return content + length
})

const infoTable = [
  { bg: 'bg-gray-200', tc: 'text-gray-500', tk: 'password-strength.very-weak' },
  { bg: 'bg-red-600 dark:bg-red-700', tc: 'text-red-700 dark:text-red-600', tk: 'password-strength.weak' },
  { bg: 'bg-amber-600 dark:bg-amber-700', tc: 'text-amber-700 dark:text-amber-600', tk: 'password-strength.medium' },
  { bg: 'bg-lime-600 dark:bg-lime-700', tc: 'text-lime-700 dark:text-lime-500', tk: 'password-strength.good' },
  { bg: 'bg-green-600 dark:bg-green-700', tc: 'text-green-700 dark:text-green-500', tk: 'password-strength.strong' },
  { bg: 'bg-blue-500 dark:bg-blue-600', tc: 'text-blue-600 dark:text-blue-400', tk: 'password-strength.very-strong' },
]
</script>

<template>
  <div>
    <div class="grid grid-cols-5 h-1.5 gap-0.5 rounded-sm overflow-hidden">
      <div
        v-for="criterium in 5"
        :key="criterium"
        :class="[
          'motion-safe:transition-colors',
          criteriumMeetCount >= criterium
            ? infoTable[criteriumMeetCount].bg
            : 'bg-gray-200 dark:bg-gray-600',
        ]"
      />
    </div>
    <p
      :class="[
        'text-right mt-1 text-xs font-medium',
        infoTable[criteriumMeetCount].tc
      ]"
    >
      {{ $t(infoTable[criteriumMeetCount].tk) }}
    </p>
  </div>
</template>
