package io.github.alessandrojean.tankobon.infrastructure.parser

val DIGITS_ONLY_REGEX = "\\D".toRegex()

fun String.removeNonDigits(): String {
  return replace(DIGITS_ONLY_REGEX, "")
}

fun String.isValidBarcode(): Boolean {
  return isValidIsbn() || isValidIssn() || isValidEan()
}

enum class CodeType {
  ISBN_13,
  ISBN_10,
  ISSN,
  EAN_13,
  UNKNOWN,
}

fun String.guessCodeType(): CodeType {
  val digitsOnly = removeNonDigits()

  return when {
    isValidIsbn() -> if (digitsOnly.length == 13) CodeType.ISBN_13 else CodeType.ISBN_10
    isValidIssn() -> CodeType.ISSN
    isValidEan() -> CodeType.EAN_13
    else -> CodeType.UNKNOWN
  }
}
