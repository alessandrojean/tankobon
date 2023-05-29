package io.github.alessandrojean.tankobon.infrastructure.parser

val DIGITS_ONLY_REGEX = "\\D".toRegex()

fun String.removeNonDigits(): String {
  return replace(DIGITS_ONLY_REGEX, "")
}

fun String.isValidBarcode(): Boolean {
  return isValidIsbn() || isValidIssn() || isValidEan()
}

interface CodeType

enum class CodeTypeIsbn : CodeType {
  ISBN_13,
  ISBN_10,
}

enum class CodeTypeOther : CodeType {
  ISSN,
  EAN_13,
  UNKNOWN,
}

fun String.guessCodeType(): CodeType {
  val digitsOnly = removeNonDigits()

  return when {
    isValidIsbn() -> if (digitsOnly.length == 13) CodeTypeIsbn.ISBN_13 else CodeTypeIsbn.ISBN_10
    isValidIssn() -> CodeTypeOther.ISSN
    isValidEan() -> CodeTypeOther.EAN_13
    else -> CodeTypeOther.UNKNOWN
  }
}
