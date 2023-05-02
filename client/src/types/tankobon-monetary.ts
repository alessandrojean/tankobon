export interface MonetaryAmount {
  amount: number
  currency: string
}

/**
 * Mainly used for inputs where it's needed to
 * validate the amount value as it is.
 */
export interface MonetaryAmountString {
  amount: string
  currency: string
}
