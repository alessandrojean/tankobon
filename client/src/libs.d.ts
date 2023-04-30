declare module 'markdown-it-abbr' {
  import type { PluginSimple } from 'markdown-it'
  const abbr: PluginSimple
  export default abbr
}

declare module 'markdown-it-implicit-figures' {
  import type { PluginWithOptions } from 'markdown-it'
  type Options = {
    dataType?: boolean
    figcaption?: boolean
    tabindex?: boolean
    link?: boolean
    copyAttrs?: boolean
  }
  const implicitFigures: PluginWithOptions<Options>
  export default implicitFigures
}
