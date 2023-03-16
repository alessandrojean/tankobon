<template>
  <div>
    <Header :title="$t('users.header')" class="mb-3 md:mb-0" />
    <div class="p-4 sm:p-6">
      <UsersTable />
    </div>
  </div>
</template>

<route lang="yaml">
  meta:
    layout: dashboard
    isAdminOnly: true
</route>
