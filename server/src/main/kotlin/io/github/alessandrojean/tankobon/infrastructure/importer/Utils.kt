package io.github.alessandrojean.tankobon.infrastructure.importer

fun String.removeDashes(): String {
  return replace("-", "")
}

fun String.toIsbn10(): String? {
  val onlyDigits = removeDashes()

  if (onlyDigits.length == 10) {
    return onlyDigits
  }

  val equalPart = substring(3).dropLast(1)
  val sum = equalPart
    .mapIndexed { i, digit -> digit.digitToInt(10) * (i + 1) }
    .sum()
  val lastDigit = sum % 11

  return equalPart + (if (lastDigit == 10) "X" else lastDigit.toString())
}

fun String.toIsbn13(): String? {
  val onlyDigits = removeDashes()

  if (onlyDigits.length == 13) {
    return onlyDigits
  }

  val newIsbn = "978${dropLast(1)}"
  val sum = newIsbn
    .mapIndexed { i, digit -> digit.digitToInt(10) * (if ((i + 1) % 2 == 0) 3 else 1) }
    .sum()
  val lastDigit = sum % 10

  return newIsbn + (if (lastDigit != 0) 10 - lastDigit else 0)
}
