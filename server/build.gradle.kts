import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  kotlin("plugin.spring")
  kotlin("kapt")

  id("org.springframework.boot") version "3.0.3"
  id("io.spring.dependency-management") version "1.1.0"
  id("nu.studer.jooq") version "8.1"
  id("org.flywaydb.flyway") version "9.15.1"
  id("org.springdoc.openapi-gradle-plugin") version "1.6.0"
  id("org.jlleitschuh.gradle.ktlint") version "11.3.2"
}

group = "io.github.alessandrojean"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.1")

  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-artemis")
  implementation("org.springframework.boot:spring-boot-starter-jooq")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.data:spring-data-commons")
  implementation("org.springframework.session:spring-session-core")
  implementation("com.github.gotson:spring-session-caffeine:2.0.0")

  kapt("org.springframework.boot:spring-boot-configuration-processor")
  developmentOnly("org.springframework.boot:spring-boot-devtools")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

  implementation("org.flywaydb:flyway-core")
  implementation("org.apache.activemq:artemis-jms-server:2.28.0")
  implementation("com.github.ben-manes.caffeine:caffeine:3.1.5")
  implementation("net.coobird:thumbnailator:0.4.19")
  implementation("org.apache.tika:tika-core:2.7.0")
  implementation("io.trbl:blurhash:1.0.0")

  runtimeOnly("com.twelvemonkeys.imageio:imageio-webp:3.9.4")

  implementation("org.xerial:sqlite-jdbc:3.41.0.0")
  jooqGenerator("org.xerial:sqlite-jdbc:3.41.0.0")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(module = "mockito-core")
  }
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.ninja-squad:springmockk:4.0.2")
  testImplementation("io.mockk:mockk:1.13.4")

  compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")

  implementation("com.ibm.icu:icu4j:72.1")
  implementation("io.github.microutils:kotlin-logging-jvm:2.1.23") // 3.0 brings SLF4J 2

  implementation("org.javamoney:moneta:1.4.2")
  implementation("org.zalando:jackson-datatype-money:1.3.0")

  val springdocVersion = "2.0.2"
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:$springdocVersion")
  implementation("org.springdoc:springdoc-openapi-starter-common:$springdocVersion")

  val luceneVersion = "9.5.0"
  implementation("org.apache.lucene:lucene-core:$luceneVersion")
  implementation("org.apache.lucene:lucene-analysis-common:$luceneVersion")
  implementation("org.apache.lucene:lucene-queryparser:$luceneVersion")
}

val client = "$rootDir/client"

tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf(
        "-Xjsr305=strict",
        "-opt-in=kotlin.time.ExperimentalTime",
      )
      jvmTarget = JavaVersion.VERSION_17.toString()
    }
  }

  withType<Test> {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
    maxHeapSize = "1G"
  }

  withType<ProcessResources> {
    filesMatching("application*.yml") {
      expand(project.properties + mapOf("rootDir" to rootDir))
    }
  }

  getByName<Jar>("jar") {
    enabled = false
  }

  // Use a known archive name for Docker builds
  if (System.getenv("DOCKER_PIPELINE").toBoolean()) {
    bootJar.configure {
      archiveFileName.set("tankobon.jar")
    }
  }

  register<Exec>("pnpmInstall") {
    group = "client"
    workingDir(client)
    inputs.file("$client/package.json")
    outputs.dir("$client/node_modules")
    commandLine(
      if (Os.isFamily(Os.FAMILY_WINDOWS)) "pnpm.cmd" else "pnpm",
      "install",
    )
  }

  register<Exec>("pnpmBuild") {
    group = "client"
    dependsOn("pnpmInstall")
    workingDir(client)
    inputs.dir(client)
    outputs.dir("$client/dist")
    commandLine(
      if (Os.isFamily(Os.FAMILY_WINDOWS)) "pnpm.cmd" else "pnpm",
      "build",
    )
  }

  register<Sync>("copyWebDist") {
    group = "client"
    dependsOn("pnpmBuild")
    from("$client/dist/")
    into("$projectDir/src/main/resources/public/")
  }
}

springBoot {
  buildInfo {
    properties {
      inputs.file("$rootDir/gradle.properties")
    }
  }
}

sourceSets {
  val flyway by creating {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
  }

  main {
    output.dir(flyway.output)
  }
}

val dbSqlite = mapOf(
  "url" to "jdbc:sqlite:${project.buildDir}/generated/flyway/database.sqlite",
)

val migrationDirsSqlite = listOf(
  file("$projectDir/src/flyway/resources/db/migration/sqlite"),
  file("$projectDir/src/flyway/kotlin/db/migration/sqlite"),
).filter(File::exists)

flyway {
  url = dbSqlite["url"]
  locations = arrayOf("classpath:db/migration/sqlite")
}

tasks.flywayMigrate {
  dependsOn("flywayClasses")
  migrationDirsSqlite.forEach(inputs::dir)
  outputs.dir("${project.buildDir}/generated/flyway")
  doFirst {
    delete(outputs.files)
    mkdir("${project.buildDir}/generated/flyway")
  }
  mixed = true
}

jooq {
  configurations {
    create("main") {
      jooqConfiguration.apply {
        logging = org.jooq.meta.jaxb.Logging.WARN
        jdbc.apply {
          driver = "org.sqlite.JDBC"
          url = dbSqlite["url"]
        }

        generator.apply {
          database.apply {
            name = "org.jooq.meta.sqlite.SQLiteDatabase"
          }
          target.apply {
            packageName = "io.github.alessandrojean.tankobon.jooq"
          }
        }
      }
    }
  }
}

tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
  migrationDirsSqlite.forEach(inputs::dir)
  allInputsDeclared.set(true)
  dependsOn("flywayMigrate")
}

openApi {
  outputDir.set(file("$projectDir/docs"))
  customBootRun {
    args.add("--spring.profiles.active=claim")
  }
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
  filter { exclude("**/db/migration/**") }
}
