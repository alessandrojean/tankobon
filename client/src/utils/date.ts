export function convertUtcToLocalTimeZone(iso8601: string) {
  const date = new Date(iso8601)
  const YYYY = date.getFullYear()
  const MM = String(date.getMonth() + 1).padStart(2, '0')
  const DD = String(date.getDate()).padStart(2, '0')
  const HH = String(date.getHours()).padStart(2, '0')
  const II = String(date.getMinutes()).padStart(2, '0')

  return `${YYYY}-${MM}-${DD}T${HH}:${II}`
}

export function convertDateUtcToLocalTimeZone(iso8601: string) {
  const date = new Date(iso8601)
  const YYYY = date.getFullYear()
  const MM = String(date.getMonth() + 1).padStart(2, '0')
  const DD = String(date.getDate()).padStart(2, '0')

  return `${YYYY}-${MM}-${DD}`
}

export function convertLocalTimeZoneToUtc(iso8601: string) {
  return new Date(iso8601).toISOString()
}

export function convertLocalTimeZoneDateToUtc(iso8601: string) {
  return convertLocalTimeZoneToUtc(iso8601).substring(0, 10)
}
