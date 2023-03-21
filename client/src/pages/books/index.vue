<script lang="ts" setup>
import { ArrowDownOnSquareIcon } from '@heroicons/vue/20/solid';

</script>

<route lang="yaml">
  meta:
    layout: dashboard
</route>

<template>
  <div>
    <Header :title="$t('entities.books')">
      <template #actions>
        <Button
          kind="primary"
          is-router-link
          :to="{ name: 'import-search' }"
        >
          <ArrowDownOnSquareIcon class="w-5 h-5" />
          <span>{{ $t('common-actions.import') }}</span>
        </Button>
      </template>
    </Header>
  </div>
</template>
