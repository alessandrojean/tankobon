<script lang="ts" setup>
import type { ErrorObject } from '@vuelidate/core'
import { EyeIcon, LinkIcon, ListBulletIcon, PencilIcon } from '@heroicons/vue/20/solid'
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

  if (showPreview)
    markdownContent.value = renderMarkdown(modelValue.value)
}

const mdBold = ref<HTMLElement>()
const mdItalic = ref<HTMLElement>()
const mdStrikethrough = ref<HTMLElement>()
const mdUnorderedList = ref<HTMLElement>()
const mdLink = ref<HTMLElement>()

const characterCount = computed(() => modelValue.value.length)
/**
 * We create a wrapper around the custom element as Vue's
 * TypeScript with Volar doesn't seem to work well with it
 * and doesn't provide a way to extend the <template> types.
 */
// const MarkdownToolbar = defineComponent({
//   props: { for: String },
//   template: '<markdown-toolbar :for="$props.for"><slot/></markdown-toolbar>'
// })

// const MdBold = defineComponent({ template: '<md-bold />' })
// const MdItalic = defineComponent({ template: '<md-italic />' })
// const MdLink = defineComponent({ template: '<md-link />' })
// const MdStrikethrough = defineComponent({ template: '<md-strikethrough />' })
// const MdUnorderedList = defineComponent({ template: '<md-unordered-list />' })
</script>

<script lang="ts">
export default { components: { LinkIcon, EyeIcon, PencilIcon, ListBulletIcon }, inheritAttrs: false }
</script>

<template>
  <div
    class="border bg-white dark:bg-gray-950 shadow-sm rounded-md overflow-hidden border-gray-300 dark:border-gray-700"
  >
    <fieldset
      class="flex flex-col group"
      :aria-disabled="attributes.disabled"
      :aria-describedby="`${attributes.id}-description`"
    >
      <label
        class="font-medium text-xs px-3 pt-3 select-none" :class="[
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
          kind="ghost-alt"
          size="mini"
          class="w-8 h-8"
          :title="showPreview ? $t('markdown-input.edit') : $t('markdown-input.preview')"
          :aria-label="showPreview ? $t('markdown-input.edit') : $t('markdown-input.preview')"
          @click="togglePreview"
        >
          <span class="sr-only">
            {{ showPreview ? $t('markdown-input.edit') : $t('markdown-input.preview') }}
          </span>
          <PencilIcon v-if="showPreview" class="w-4 h-4" />
          <EyeIcon v-else class="w-4 h-4" />
        </Button>

        <Toolbar
          v-if="!showPreview"
          class="flex gap-1 ml-auto"
        >
          <Button
            kind="ghost-alt"
            size="mini"
            class="w-8 h-8"
            :title="$t('markdown-input.bold')"
            @click="mdBold?.click()"
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
            @click="mdItalic?.click()"
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
            @click="mdStrikethrough?.click()"
          >
            <span class="sr-only">{{ $t('markdown-input.striketrough') }}</span>
            <span
              aria-hidden="true"
              class="text-base/4 font-medium line-through w-4 h-4"
            >
              {{ $t('markdown-input.striketrough-letter') }}
            </span>
          </Button>
          <Button
            kind="ghost-alt"
            size="small"
            class="w-8 h-8"
            :title="$t('markdown-input.unordered-list')"
            @click="mdUnorderedList?.click()"
          >
            <span class="sr-only">{{ $t('markdown-input.unordered-list') }}</span>
            <ListBulletIcon class="w-5 h-5" />
          </Button>
          <Button
            kind="ghost-alt"
            size="small"
            class="w-8 h-8"
            :title="$t('markdown-input.link')"
            @click="mdLink?.click()"
          >
            <span class="sr-only">{{ $t('markdown-input.link') }}</span>
            <LinkIcon class="w-5 h-5" />
          </Button>
        </Toolbar>
      </div>

      <div class="pb-2.5">
        <div v-if="!showPreview" class="px-3">
          <textarea
            v-bind="$attrs" ref="textarea"
            class="bg-white dark:bg-gray-950 min-h-[9rem] border-0 rounded-md focus:outline-none placeholder:text-gray-500 w-full font-mono text-sm/6 motion-safe:transition"
            :class="[
              invalid
                ? 'ring-1 ring-red-500 dark:ring-red-500/95'
                : 'group-hover:ring-1 group-hover:ring-gray-300 dark:group-hover:ring-gray-700 focus:ring-1 focus:ring-primary-500 dark:focus:ring-primary-500',
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
              class="w-8 h-8"
              :title="$t('markdown-input.guide')"
            >
              <span class="sr-only">
                {{ $t('markdown-input.guide') }}
              </span>
              <svg aria-hidden="true" class="w-4 h-4 fill-current" viewBox="0 0 16 16" version="1.1">
                <path fill-rule="evenodd" d="M14.85 3H1.15C.52 3 0 3.52 0 4.15v7.69C0 12.48.52 13 1.15 13h13.69c.64 0 1.15-.52 1.15-1.15v-7.7C16 3.52 15.48 3 14.85 3zM9 11H7V8L5.5 9.92 4 8v3H2V5h2l1.5 2L7 5h2v6zm2.99.5L9.5 8H11V5h2v3h1.5l-2.51 3.5z" data-v-c67b3c24="" />
              </svg>
            </Button>

            <span v-if="characterCount > 0" class="select-none ml-auto block text-xs tabular-nums text-gray-500 dark:text-gray-400">
              {{ characterCount }}
            </span>
          </div>
        </div>
        <div v-else class="pb-3" aria-live="polite">
          <em v-if="modelValue.length === 0" class="text-sm px-3">
            {{ $t('markdown-input.no-preview') }}
          </em>
          <div
            v-else
            class="prose prose- px-6 py-1 prose-sm lg:prose-base max-w-none lg:max-w-none dark:prose-invert"
            v-html="markdownContent"
          />
        </div>
      </div>
    </fieldset>

    <p
      v-if="invalid && errorMessage"
      class="text-red-700 dark:text-red-500/95 text-sm font-medium ml-2 mt-1 mb-4"
    >
      {{ errorMessage }}
    </p>

    <markdown-toolbar :for="String($attrs.id)" class="hidden">
      <md-bold ref="mdBold" />
      <md-italic ref="mdItalic" />
      <md-link ref="mdLink" />
      <md-strikethrough ref="mdStrikethrough" />
      <md-unordered-list ref="mdUnorderedList" />
    </markdown-toolbar>
  </div>
</template>
