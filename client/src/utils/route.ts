interface SafeNumberOptions {
  min?: number
  max?: number
}

export function safeNumber(string: string, defaultValue = 0, options: SafeNumberOptions = {}): number {
  const number = Number(string)

  if (isNaN(number)) {
    return defaultValue
  }

  const atLeast = options.min !== undefined ? Math.max(options.min, number) : number
  const atMost = options.max !== undefined ? Math.min(options.max, atLeast) : atLeast

  return atMost
}
