package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.ServerAlreadyClaimedException
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.service.TankobonUserLifecycle
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ClaimDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ClaimStatusDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.UserEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("api/v1/claim", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(
  name = "Claim",
  description = """
The claim feature can be used in a new Tankobon instance that needs to set its first
administrator account. By claiming a server, the user will be given an administrator role
and will be able to create new users and entities. After a server has been claimed,
it can not be claimed by other user any more.
  """,
)
class ClaimController(private val userLifecycle: TankobonUserLifecycle) {

  @GetMapping
  @Operation(summary = "Get the server claim status")
  fun getClaimStatus() = ClaimStatusDto(userLifecycle.countUsers() > 0)

  @PostMapping
  @Operation(summary = "Claim the server")
  fun claimAdmin(@RequestBody claimInfo: ClaimDto): SuccessEntityResponseDto<UserEntityDto> {
    if (userLifecycle.countUsers() > 0) {
      throw ServerAlreadyClaimedException()
    }

    val admin = userLifecycle.createUser(
      TankobonUser(
        email = claimInfo.email,
        password = claimInfo.password,
        isAdmin = true,
        name = claimInfo.name,
      ),
    )

    return SuccessEntityResponseDto(admin.toDto())
  }
}
