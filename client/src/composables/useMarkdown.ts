import md, { type PluginSimple } from 'markdown-it'
import mdAbbr from 'markdown-it-abbr'
import mdImplicitFigures from 'markdown-it-implicit-figures'
import mdLinkAttributes from 'markdown-it-link-attributes'
import dedent from 'dedent'

/**
 * Adapted from markdown-it-video
 * https://github.com/CenterForOpenScience/markdown-it-video
 */
const video: PluginSimple = (md) => {
  const ytRegex = /^https?:\/\/(www\.)?youtu\.?be(\.com)?\/(watch|embed|playlist)?(\?(?:v|list)=|\/)?([^$/\n]+)/
  const EMBED_REGEX = /@youtube\([\s]*(.*?)[\s]*[)]/im

  function youtubeParser(url: string) {
    const matches = url.match(ytRegex)
    return matches ? { id: matches[5], type: matches[3] } : { id: url, type: 'video' }
  }

  md.renderer.rules.video = (tokens, idx) => {
    const id = md.utils.escapeHtml(tokens[idx].attrGet('videoId') ?? '')
    const type = md.utils.escapeHtml(tokens[idx].attrGet('videoType') ?? '')
    const endpoint = type === 'playlist' ? 'videoseries?list=' : ''

    if (id.length === 0) {
      return ''
    }

    const thumbnail = dedent`
      <img
        src="https://img.youtube.com/vi/${id}/maxresdefault.jpg"
        class="w-full h-full !my-0 object-cover blur scale-105 opacity-80"
        aria-hidden="true"
      >
    `

    return dedent`
      <div class="video-player aspect-[16/9]">
        ${type !== 'playlist' ? thumbnail : ''}
        <iframe
          src="https://www.youtube-nocookie.com/embed/${endpoint}${id}"
          title="YouTube video player"
          frameborder="0"
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
          allowfullscreen
          loading="lazy"
        >
        </iframe>
      </div>
    `
  }

  md.block.ruler.before('paragraph', 'video', (state, startLine, endLine, silent) => {
    const nextLine = startLine + 1
    const content = state.getLines(startLine, nextLine, state.blkIndent, false).trim()

    if (!content.startsWith('@youtube(')) {
      return false
    }

    const match = EMBED_REGEX.exec(content)

    if (!match) {
      return false
    }

    const video = youtubeParser(match[1])

    if (!video) {
      return false
    }

    state.line = nextLine + 1

    if (!silent) {
      state.push('video_open', 'figure', 1)

      const token = state.push('video', '', 0)
      token.attrSet('videoId', video.id)
      token.attrSet('videoType', video.type)
      token.level = state.level

      state.push('video_close', 'figure', -1)
    }

    return true
  })
}

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
    .use(video)
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
      'html_inline',
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
