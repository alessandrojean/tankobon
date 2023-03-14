import tailwind from 'tailwindcss'
import nesting from 'tailwindcss/nesting'
import autoprefixer from 'autoprefixer'
import tailwindConfig from './tailwind.config.js'

export default {
  plugins: [nesting, tailwind(tailwindConfig), autoprefixer]
}
