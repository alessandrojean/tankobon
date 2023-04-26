package io.github.alessandrojean.tankobon.infrastructure.parser

val EAN_REGEX = "^\\d{13}$".toRegex()

fun String.isValidEan(): Boolean {
  val onlyDigits = removeNonDigits()

  if (!onlyDigits.matches(EAN_REGEX)) {
    return false
  }

  val checkSum = onlyDigits
    .dropLast(1)
    .mapIndexed { index, c -> c.digitToInt(10) * (if ((index + 1) % 2 == 0) 3 else 1) }
    .sum()

  return (10 - (checkSum % 10)) % 10 == onlyDigits.last().digitToInt(10)
}
