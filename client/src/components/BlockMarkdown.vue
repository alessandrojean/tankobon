<script setup lang="ts">
import type { UseMarkdownOptions } from '@/composables/useMarkdown'
import Block from '@/components/Block.vue'

export interface BlockMarkdownProps {
  blur?: boolean
  emptyMessage?: string
  loading?: boolean
  markdown?: string
  options?: UseMarkdownOptions
  title: string
}

const props = withDefaults(defineProps<BlockMarkdownProps>(), {
  emptyMessage: undefined,
  markdown: undefined,
  blur: false,
  loading: false,
  options: () => ({}),
})

const { t } = useI18n()
const { markdown, blur, loading, title, emptyMessage, options } = toRefs(props)
const { renderMarkdown } = useMarkdown(options.value)

const markdownRendered = computed(() => {
  if (loading.value) {
    return ''
  }

  if (markdown.value!.length === 0) {
    return emptyMessage.value ? `<em>${emptyMessage.value}</em>` : ''
  }

  return renderMarkdown(markdown.value!)
})

const collapsed = ref(true)
const showReadMore = ref(false)
const content = ref<InstanceType<typeof Block>>()
const breakpoint = 208 // h-52

async function handleReadMore() {
  await nextTick()

  if (loading.value) {
    collapsed.value = true
    showReadMore.value = false
  } else {
    showReadMore.value = (content.value?.$el?.offsetHeight ?? 0) > breakpoint
    collapsed.value = showReadMore.value
  }
}

watch([markdownRendered, loading], handleReadMore)
onMounted(handleReadMore)

function toggleCollapsed() {
  if (showReadMore.value) {
    collapsed.value = !collapsed.value
  }
}

function handleContainerClick(event: MouseEvent) {
  if (collapsed.value) {
    event.preventDefault()
    toggleCollapsed()
  }
}
</script>

<template>
  <Block
    v-if="markdownRendered?.length || emptyMessage || loading"
    ref="content" class="block-markdown relative motion-safe:transition overflow-hidden"
    :class="[
      collapsed && showReadMore
        ? 'h-52 cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 animate-hover'
        : '',
    ]"
    :title="title"
    @click="handleContainerClick"
  >
    <div
      v-if="!loading"
      :class="{
        'spoiler-blur':
          blur && (collapsed || !showReadMore) && markdownRendered?.length,
        'show-on-hover': blur && !showReadMore && markdownRendered?.length,
      }"
      class="mt-3 prose prose-sm sm:prose-base dark:prose-invert max-w-none motion-safe:transition"
      :inert="showReadMore && collapsed"
      v-html="markdownRendered"
    />
    <div v-else class="flex flex-col space-y-2 mt-3" aria-hidden="true">
      <div class="skeleton w-full h-5" />
      <div class="skeleton w-full h-5" />
      <div class="skeleton w-full h-5" />
      <div class="skeleton w-full h-5" />
      <div class="skeleton w-full h-5" />
      <div class="skeleton w-6/12 h-5" />
    </div>

    <FadeTransition>
      <div
        v-if="showReadMore"
        class="inset-x-0 bottom-0 flex justify-center items-end motion-safe:transition-colors"
        :class="[
          collapsed ? 'h-1/2 collapsed-gradient absolute p-2' : 'pt-2',
        ]"
      >
        <Button
          kind="ghost"
          size="mini"
          rounded="full"
          :aria-expanded="collapsed"
          @click.stop="toggleCollapsed"
        >
          {{
            collapsed
              ? t('common-actions.expand')
              : t('common-actions.collapse')
          }}
        </Button>
      </div>
    </FadeTransition>
  </Block>
</template>

<style lang="postcss" scoped>
.block-markdown {
  .spoiler-blur {
    @apply md:blur-sm md:dark:blur md:select-none;

    &.show-on-hover {
      @apply md:hover:blur-none md:dark:hover:blur-none md:hover:select-auto;
    }
  }

  .collapsed-gradient {
    background-image: linear-gradient(
      to bottom,
      transparent,
      theme('colors.block / 70%') 40%,
      theme('colors.block') 80%
    );

    html.dark & {
      background-image: linear-gradient(
        to bottom,
        transparent,
        theme('colors.block-dark / 70%') 40%,
        theme('colors.block-dark') 80%
      );
    }
  }

  &.animate-hover:hover .collapsed-gradient {
    background-image: linear-gradient(
      to bottom,
      transparent,
      theme('colors.gray.100 / 70%') 40%,
      theme('colors.gray.100') 80%
    );

    html.dark & {
      background-image: linear-gradient(
        to bottom,
        transparent,
        theme('colors.gray.800 / 70%') 40%,
        theme('colors.gray.800') 80%
      );
    }
  }
}
</style>
