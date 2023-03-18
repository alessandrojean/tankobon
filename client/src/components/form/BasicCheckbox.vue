<template>
  <input
    :class="[
      'rounded w-4 h-4 dark:bg-gray-900',
      'dark:checked:bg-primary-500 dark:checked:border-primary-500',
      'dark:indeterminate:bg-primary-500 dark:indeterminate:border-primary-500',
      'border-gray-300 dark:border-gray-600',
      'shadow-sm text-primary-500 focus:border-primary-500 focus:ring',
      'dark:focus:border-primary-400 dark:focus:ring-primary-200/30',
      'focus:ring-offset-0 focus:ring-primary-200 focus:ring-opacity-50',
      'motion-safe:transition-shadow disabled:opacity-50'
    ]"
    type="checkbox"
  >
</template>
