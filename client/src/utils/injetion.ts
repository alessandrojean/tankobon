export function injectStrict<T>(key:  InjectionKey<T>, defaultValue?: T) {
  const resolved = inject(key, defaultValue)

  if (!resolved) {
    throw new Error(`Could not resolve ${key.description}`)
  }

  return resolved
}
