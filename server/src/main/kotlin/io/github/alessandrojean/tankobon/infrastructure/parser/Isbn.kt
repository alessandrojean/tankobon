package io.github.alessandrojean.tankobon.infrastructure.parser

val ISBN_REGEX = "^(978|979)\\d{10}$|^\\d{9}[0-9xX]$".toRegex()

fun String.isValidIsbn(): Boolean {
  val onlyDigits = removeNonDigits()

  if (onlyDigits.length != 10 && onlyDigits.length != 13) {
    return false
  }

  if (!onlyDigits.matches(ISBN_REGEX)) {
    return false
  }

  if (length == 10) {
    val parcels = onlyDigits.mapIndexed { i, digit ->
      (10 - i) * (if (digit == 'X' || digit == 'x') 10 else digit.digitToInt(10))
    }

    return parcels.sum() % 11 == 0
  }

  val parcels = onlyDigits.mapIndexed { i, digit ->
    digit.digitToInt(10) * (if ((i + 1) % 2 == 0) 3 else 1)
  }

  return parcels.sum() % 10 == 0
}

fun String.toIsbn10(): String? {
  if (!isValidIsbn()) {
    return null
  }

  val onlyDigits = removeNonDigits()

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

data class IsbnInformation(
  val group: Int,
  val region: String,
  val language: String
)

val RegistrationGroups = arrayOf(
  IsbnInformation(0, "US", "en"),
  IsbnInformation(1, "US", "en"),
  IsbnInformation(2, "FR", "fr"),
  IsbnInformation(3, "DE", "de"),
  IsbnInformation(4, "JP", "ja"),
  IsbnInformation(5, "RU", "ru"),
  IsbnInformation(7, "CN", "zh"),
  IsbnInformation(65, "BR", "pt-BR"),
  IsbnInformation(84, "ES", "es"),
  IsbnInformation(85, "BR", "pt-BR"),
  IsbnInformation(88, "IT", "it"),
  IsbnInformation(89, "KR", "ko"),
  IsbnInformation(607, "MX", "es"),
  IsbnInformation(612, "PE", "es"),
  IsbnInformation(950, "AR", "es"),
  IsbnInformation(956, "CL", "es"),
  IsbnInformation(958, "CO", "es"),
  IsbnInformation(968, "MX", "es"),
  IsbnInformation(970, "MX", "es"),
  IsbnInformation(972, "PT", "pt"),
  IsbnInformation(987, "AR", "es"),
  IsbnInformation(989, "PT", "pt"),
  IsbnInformation(9915, "UY", "es"),
  IsbnInformation(9917, "BO", "es"),
  IsbnInformation(9946, "KP", "ko"),
  IsbnInformation(9972, "PE", "es"),
  IsbnInformation(9974, "UY", "es"),
  IsbnInformation(99905, "BO", "es"),
  IsbnInformation(99954, "BO", "es"),
  IsbnInformation(99974, "BO", "es")
)

fun String.toIsbnInformation(): IsbnInformation? {
  val onlyDigits = removeNonDigits()

  if (!onlyDigits.isValidIsbn()) {
    return null
  }

  val usefulPart = if (onlyDigits.length == 13) {
    onlyDigits.substring(3)
  } else {
    onlyDigits
  }

  for (i in (1..5)) {
    val group = usefulPart.substring(0, i)
    val info = RegistrationGroups.firstOrNull { it.group.toString() == group }

    if (info != null) {
      return info
    }
  }

  return null
}