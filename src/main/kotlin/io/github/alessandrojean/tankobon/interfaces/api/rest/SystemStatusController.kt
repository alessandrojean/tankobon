package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SystemStatus
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SystemStatusDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.core.env.Environment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.info.BuildProperties

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "system-status", description = "System status and statistics")
class SystemStatusController(
  @Autowired private val environment: Environment,
  @Autowired private val buildProperties: BuildProperties,
) {

  @GetMapping("api/v1/system-status")
  @Operation(summary = "Get the system status")
  fun getSystemStatus(): SystemStatusDto = SystemStatusDto(
    status = SystemStatus.OPERATIONAL,
    isDevelopment = environment.activeProfiles.contains("dev"),
    isDemo = environment.activeProfiles.contains("demo"),
    version = buildProperties.version
  )

}
