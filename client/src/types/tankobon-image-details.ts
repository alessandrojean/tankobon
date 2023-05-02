export interface ImageDetailsAttributes {
  fileName: string
  versions: Record<string, string>
  width: number
  height: number
  aspectRatio: string
  format: string
  mimeType: string
  timeHex: string
  blurHash: string
}
