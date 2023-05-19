<script setup lang="ts">
import { TabList } from '@headlessui/vue';
import Button from './form/Button.vue';

export interface PillTab {
  key: string
  text: string
  icon: Component
  disabled?: boolean
}

export interface PillTabsListProps {
  disabled?: boolean
  tabs: PillTab[]
}

const props = withDefaults(defineProps<PillTabsListProps>(), {
  disabled: false,
})

const { tabs } = toRefs(props)
const activeTab = defineModel<PillTab>({ required: true })

const tabList = ref<InstanceType<typeof TabList>>()
const tabBackground = reactive({ left: 0, width: 0 })
const mounted = ref(false)

async function calculateTabBackground() {
  await nextTick()
  const tabListElement = tabList.value?.$el as HTMLDivElement
  const activeTabElement = tabListElement?.querySelector<HTMLButtonElement>('[aria-selected=true]')

  tabBackground.left = activeTabElement?.offsetLeft ?? 0
  tabBackground.width = activeTabElement?.offsetWidth ?? 0
}

watch(activeTab, calculateTabBackground, { immediate: true })
onMounted(() => {
  calculateTabBackground()
  setTimeout(() => {
    mounted.value = true
  }, 250)
})

const disabledTabs = computed(() => {
  return tabs.value
    .map((t, i) => t.disabled ? i : -1)
    .filter(i => i !== -1)
})
</script>

<template>
  <div>
    <TabList ref="tabList" class="hidden md:flex items-center gap-2 relative">
      <div
        aria-hidden="true"
        :class="[
          'w-[--width] h-full rounded-md bg-primary-100 dark:bg-primary-900',
          'absolute left-0 top-0 motion-safe:transition-[width,transform]',
          'translate-x-[--offset]',
          { 'motion-safe:duration-0': !mounted },
        ]"
        :style="{
          '--offset': `${tabBackground.left}px`,
          '--width': `${tabBackground.width}px`,
        }"
      />
      <Tab
        v-for="tab in tabs"
        :key="String(tab.key)"
        :as="Button"
        kind="pill-tab"
        size="pill-tab"
        :disabled="disabled || tab.disabled"
      >
        <component :is="tab.icon" class="w-5 h-5" />
        <span>{{ $t(tab.text) }}</span>
      </Tab>
    </TabList>

    <BasicSelect
      v-model="activeTab"
      class="md:hidden h-12"
      :options="tabs"
      :option-text="(tab: any) => $t(tab.text)"
      :option-value="(tab: any) => tab.key"
      :disabled="disabled"
      :disabled-options="disabledTabs"
    />
  </div>
</template>
