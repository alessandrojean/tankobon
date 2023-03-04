package io.github.alessandrojean.tankobon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TankobonApplication

fun main(args: Array<String>) {
  System.setProperty("org.jooq.no-logo", "true")
  System.setProperty("org.jooq.no-tips", "true")
  runApplication<TankobonApplication>(*args)
}
