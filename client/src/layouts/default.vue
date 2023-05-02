<template>
  <RouterView v-slot="{ Component }">
    <FadeTransition>
      <Component :is="Component" />
    </FadeTransition>
  </RouterView>
</template>
