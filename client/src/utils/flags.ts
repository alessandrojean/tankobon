export function createFlagUrl(region: string, type: 'rectangle' | 'scripts') {
  return `/flags/${type}/${region}.svg`
}
