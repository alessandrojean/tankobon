<script lang="ts" setup>
import type { ErrorObject } from '@vuelidate/core'
import { EyeIcon, LinkIcon, PencilIcon } from '@heroicons/vue/20/solid'
import type { TextareaHTMLAttributes } from 'vue'

export interface TextInputProps {
  errors?: ErrorObject[]
  invalid?: boolean
  modelValue: string
  labelText: string
  autoComplete?: HTMLTextAreaElement['autocomplete']
  placeholder?: string
  required?: boolean
}

const props = withDefaults(defineProps<TextInputProps>(), {
  errors: undefined,
  invalid: false,
  required: false,
})

defineEmits<{
  (e: 'update:modelValue', modelValue: string): void
}>()

const { errors, modelValue, required } = toRefs(props)

const errorMessage = computed(() => errors.value?.[0]?.$message)
const attributes: TextareaHTMLAttributes = useAttrs()

const { renderMarkdown } = useMarkdown()
const markdownContent = ref(renderMarkdown(modelValue.value))
const showPreview = ref(false)
const textarea = ref<HTMLTextAreaElement>()

function togglePreview() {
  showPreview.value = !showPreview.value

  if (showPreview) {
    markdownContent.value = renderMarkdown(modelValue.value)
  }
}

const mdBoldRef = ref<HTMLElement>()
const mdItalicRef = ref<HTMLElement>()
const mdStrikethroughRef = ref<HTMLElement>()
const mdUnorderedListRef = ref<HTMLElement>()
const mdOrderedListRef = ref<HTMLElement>()
const mdLinkRef = ref<HTMLElement>()

const characterCount = computed(() => modelValue.value.length)
</script>

<script lang="ts">
export default { components: { LinkIcon, EyeIcon, PencilIcon }, inheritAttrs: false }
</script>

<template>
  <fieldset
    class="border bg-white dark:bg-gray-950 shadow-sm rounded-md overflow-hidden border-gray-300 dark:border-gray-700 disabled:opacity-60 motion-safe:transition"
  >
    <div
      class="flex flex-col group"
      :aria-disabled="attributes.disabled"
      :aria-describedby="`${attributes.id}-description`"
    >
      <label
        class="font-medium text-xs px-3 pt-3 select-none"
        :class="[
          invalid ? 'text-red-800 dark:text-red-600' : 'text-gray-700 dark:text-gray-300',
        ]"
        :for="($attrs.id as string | undefined)"
      >
        {{ labelText }}
      </label>

      <div
        :id="`${attributes.id}-description`"
        class="sr-only"
        aria-live="polite"
      >
        {{
          $t('markdown-input.description', [
            showPreview ? $t('markdown-input.preview-mode') : $t('markdown-input.edit-mode'),
          ])
        }}
      </div>

      <div class="px-3 pb-3 pt-3 flex gap-1 items-center">
        <Button
          class="h-8"
          kind="ghost-alt"
          size="mini"
          :title="showPreview ? $t('markdown-input.edit') : $t('markdown-input.preview')"
          :aria-label="showPreview ? $t('markdown-input.edit') : $t('markdown-input.preview')"
          @click="togglePreview"
        >
          <PencilIcon v-if="showPreview" class="w-4 h-4" />
          <EyeIcon v-else class="w-4 h-4" />
          <span>
            {{ showPreview ? $t('markdown-input.edit') : $t('markdown-input.preview') }}
          </span>
        </Button>

        <Toolbar
          v-if="!showPreview"
          class="flex gap-4 ml-auto"
        >
          <div class="flex">
            <Button
              kind="ghost-alt"
              size="mini"
              class="w-8 h-8"
              :title="$t('markdown-input.bold')"
              @click="mdBoldRef?.click()"
            >
              <span class="sr-only">{{ $t('markdown-input.bold') }}</span>
              <span
                aria-hidden="true"
                class="text-base/4 font-extrabold w-4 h-4"
              >
                {{ $t('markdown-input.bold-letter') }}
              </span>
            </Button>
            <Button
              kind="ghost-alt"
              size="small"
              class="w-8 h-8"
              :title="$t('markdown-input.italic')"
              @click="mdItalicRef?.click()"
            >
              <span class="sr-only">{{ $t('markdown-input.italic') }}</span>
              <span
                aria-hidden="true"
                class="text-base/4 font-medium italic w-4 h-4 [font-feature-settings:'cv08']"
              >
                {{ $t('markdown-input.italic-letter') }}
              </span>
            </Button>
            <Button
              kind="ghost-alt"
              size="small"
              class="w-8 h-8"
              :title="$t('markdown-input.striketrough')"
              @click="mdStrikethroughRef?.click()"
            >
              <span class="sr-only">{{ $t('markdown-input.striketrough') }}</span>
              <span
                aria-hidden="true"
                class="text-base/4 font-medium line-through w-4 h-4"
              >
                {{ $t('markdown-input.striketrough-letter') }}
              </span>
            </Button>
          </div>

          <div class="flex">
            <Button
              kind="ghost-alt"
              size="small"
              class="w-8 h-8"
              :title="$t('markdown-input.unordered-list')"
              @click="mdUnorderedListRef?.click()"
            >
              <span class="sr-only">{{ $t('markdown-input.unordered-list') }}</span>
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" class="w-[1.125rem] h-[1.125rem]" fill="currentColor">
                <path d="M5.75 2.5h8.5a.75.75 0 0 1 0 1.5h-8.5a.75.75 0 0 1 0-1.5Zm0 5h8.5a.75.75 0 0 1 0 1.5h-8.5a.75.75 0 0 1 0-1.5Zm0 5h8.5a.75.75 0 0 1 0 1.5h-8.5a.75.75 0 0 1 0-1.5ZM2 14a1 1 0 1 1 0-2 1 1 0 0 1 0 2Zm1-6a1 1 0 1 1-2 0 1 1 0 0 1 2 0ZM2 4a1 1 0 1 1 0-2 1 1 0 0 1 0 2Z" />
              </svg>
            </Button>
            <Button
              kind="ghost-alt"
              size="small"
              class="w-8 h-8"
              :title="$t('markdown-input.ordered-list')"
              @click="mdOrderedListRef?.click()"
            >
              <span class="sr-only">{{ $t('markdown-input.ordered-list') }}</span>
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" class="w-[1.125rem] h-[1.125rem]" fill="currentColor">
                <path d="M5 3.25a.75.75 0 0 1 .75-.75h8.5a.75.75 0 0 1 0 1.5h-8.5A.75.75 0 0 1 5 3.25Zm0 5a.75.75 0 0 1 .75-.75h8.5a.75.75 0 0 1 0 1.5h-8.5A.75.75 0 0 1 5 8.25Zm0 5a.75.75 0 0 1 .75-.75h8.5a.75.75 0 0 1 0 1.5h-8.5a.75.75 0 0 1-.75-.75ZM.924 10.32a.5.5 0 0 1-.851-.525l.001-.001.001-.002.002-.004.007-.011c.097-.144.215-.273.348-.384.228-.19.588-.392 1.068-.392.468 0 .858.181 1.126.484.259.294.377.673.377 1.038 0 .987-.686 1.495-1.156 1.845l-.047.035c-.303.225-.522.4-.654.597h1.357a.5.5 0 0 1 0 1H.5a.5.5 0 0 1-.5-.5c0-1.005.692-1.52 1.167-1.875l.035-.025c.531-.396.8-.625.8-1.078a.57.57 0 0 0-.128-.376C1.806 10.068 1.695 10 1.5 10a.658.658 0 0 0-.429.163.835.835 0 0 0-.144.153ZM2.003 2.5V6h.503a.5.5 0 0 1 0 1H.5a.5.5 0 0 1 0-1h.503V3.308l-.28.14a.5.5 0 0 1-.446-.895l1.003-.5a.5.5 0 0 1 .723.447Z" />
              </svg>
            </Button>
            <Button
              kind="ghost-alt"
              size="small"
              class="w-8 h-8"
              :title="$t('markdown-input.link')"
              @click="mdLinkRef?.click()"
            >
              <span class="sr-only">{{ $t('markdown-input.link') }}</span>
              <LinkIcon class="w-5 h-5" />
            </Button>
          </div>
        </Toolbar>
      </div>

      <div class="pb-2.5">
        <div v-if="!showPreview" class="px-3">
          <textarea
            v-bind="$attrs" ref="textarea"
            class="bg-white dark:bg-gray-950 min-h-[9rem] border-0 rounded-md focus:outline-none placeholder:text-gray-500 w-full font-mono text-sm/6 motion-safe:transition"
            :class="[
              invalid
                ? 'ring-1 !ring-red-500 dark:!ring-red-500/95'
                : 'enabled:group-hover:ring-1 enabled:group-hover:ring-gray-300 enabled:dark:group-hover:ring-gray-700 focus:ring-1 focus:!ring-primary-500 dark:focus:!ring-primary-500',
            ]"
            :value="modelValue"
            :placeholder="placeholder"
            :required="required"
            spellcheck="false"
            data-gramm="false"
            @input="$emit('update:modelValue', ($event.target! as HTMLTextAreaElement).value)"
          />

          <div class="mt-3 flex items-center">
            <Button
              kind="ghost-alt"
              size="mini"
              is-link
              href="https://www.markdownguide.org/"
              target="_blank"
              class="h-8"
              :title="$t('markdown-input.guide')"
            >
              <svg aria-hidden="true" class="w-4 h-4 fill-current" viewBox="0 0 16 16" version="1.1">
                <path fill-rule="evenodd" d="M14.85 3H1.15C.52 3 0 3.52 0 4.15v7.69C0 12.48.52 13 1.15 13h13.69c.64 0 1.15-.52 1.15-1.15v-7.7C16 3.52 15.48 3 14.85 3zM9 11H7V8L5.5 9.92 4 8v3H2V5h2l1.5 2L7 5h2v6zm2.99.5L9.5 8H11V5h2v3h1.5l-2.51 3.5z" data-v-c67b3c24="" />
              </svg>
              <span class="font-normal text-xs">
                {{ $t('markdown-input.supported') }}
              </span>
            </Button>

            <span v-if="characterCount > 0" class="select-none ml-auto block text-xs tabular-nums text-gray-500 dark:text-gray-400">
              {{ characterCount }}
            </span>
          </div>
        </div>

        <div v-else class="pb-3" aria-live="polite">
          <div
            v-if="modelValue.length === 0"
            class="prose px-6 py-1 prose-sm lg:prose-base max-w-none lg:max-w-none dark:prose-invert"
          >
            <em>{{ $t('markdown-input.no-preview') }}</em>
          </div>
          <div
            v-else
            class="prose px-6 py-1 prose-sm lg:prose-base max-w-none lg:max-w-none dark:prose-invert"
            v-html="markdownContent"
          />
        </div>
      </div>
    </div>

    <p
      v-if="invalid && errorMessage"
      class="text-red-700 dark:text-red-500/95 text-sm font-medium ml-2 mt-1 mb-4"
    >
      {{ errorMessage }}
    </p>

    <markdown-toolbar :for="String($attrs.id)" class="hidden">
      <md-bold ref="mdBoldRef" />
      <md-italic ref="mdItalicRef" />
      <md-link ref="mdLinkRef" />
      <md-strikethrough ref="mdStrikethroughRef" />
      <md-unordered-list ref="mdUnorderedListRef" />
      <md-ordered-list ref="mdOrderedListRef" />
    </markdown-toolbar>
  </fieldset>
</template>
