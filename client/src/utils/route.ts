interface SafeNumberOptions {
  min?: number
}

export function safeNumber(string: string, defaultValue: number = 0, options: SafeNumberOptions = {}): number {
  const number = Number(string)

  if (isNaN(number)) {
    return defaultValue
  }

  return options.min !== undefined ? Math.max(options.min, number) : number
}
