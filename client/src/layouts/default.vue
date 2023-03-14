<template>
  <RouterView v-slot="{ Component }">
    <FadeTransition>
      <component :is="Component" />
    </FadeTransition>
  </RouterView>
</template>
