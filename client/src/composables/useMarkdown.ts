import md, { type PluginSimple } from 'markdown-it'
import mdAbbr from 'markdown-it-abbr'
import mdImplicitFigures from 'markdown-it-implicit-figures'
import mdLinkAttributes from 'markdown-it-link-attributes'

const imageLazyLoad: PluginSimple = (md) => {
  const defaultRenderer = md.renderer.rules.image!

  md.renderer.rules.image = (tokens, idx, options, env, self) => {
    const aIndex = tokens[idx].attrIndex('loading')

    if (aIndex < 0) {
      tokens[idx].attrPush(['loading', 'lazy'])
    } else {
      tokens[idx].attrs![aIndex][1] = 'lazy'
    }

    return defaultRenderer(tokens, idx, options, env, self)
  }
}

export interface UseMarkdownOptions {
  disable?: string[]
  mdOptions?: md.Options
}

export default function useMarkdown(options: UseMarkdownOptions = {}) {
  const markdown = md(options.mdOptions ?? {})
    .use(mdAbbr)
    .use(imageLazyLoad)
    .use(mdLinkAttributes, {
      matcher: (link: string) => /^https?:\/\//.test(link),
      attrs: {
        target: '_blank',
        rel: 'noopener',
      },
    })
    .use(mdImplicitFigures, { figcaption: true })
    .disable([
      'code',
      'fence',
      'hr',
      'html_block',
      'lheading',
      ...(options.disable ?? []),
    ])

  function renderMarkdown(source: string) {
    return source ? markdown.render(source) : null
  }

  return {
    markdown,
    renderMarkdown,
    escapeHtml: markdown.utils.escapeHtml,
  }
}
