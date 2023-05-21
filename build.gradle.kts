plugins {
  run {
    val kotlinVersion = "1.8.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("kapt") version kotlinVersion
  }
}

tasks.wrapper {
  gradleVersion = "7.6.1"
  distributionType = Wrapper.DistributionType.ALL
}

