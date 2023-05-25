package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable

data class Weight(
  val value: Float,
  val unit: MassUnit = MassUnit.KILOGRAM,
) : Serializable
