package io.github.alessandrojean.tankobon.infrastructure.parser

val ISSN_REGEX = "^\\d{8}$|^\\d{7}[0-9xX]$".toRegex()

fun String.isValidIssn(): Boolean {
  val onlyDigits = removeNonDigits()

  if (!onlyDigits.matches(ISSN_REGEX)) {
    return false
  }

  if (onlyDigits.length == 8) {
    val calcDigit = onlyDigits
      .dropLast(1)
      .mapIndexed { index, c -> (8 - index) * c.digitToInt(10) }
      .sum()
      .let { (11 - it % 11) % 11 }

    return calcDigit.digitToChar(10) == onlyDigits.last()
  }

  val calcDigit = onlyDigits
    .dropLast(1)
    .mapIndexed { index, c -> (8 - index) * c.digitToInt(10) }
    .sum()
    .let { it % 11 }
    .let { if (it != 0) 11 - it else it }
    .let { if (it == 10) 'X' else it.digitToChar(10) }

  return calcDigit == onlyDigits.last()
}