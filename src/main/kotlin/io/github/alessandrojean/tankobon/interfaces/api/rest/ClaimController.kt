package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.service.TankobonUserLifecycle
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.UserDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("api/v1/claim", produces = [MediaType.APPLICATION_JSON_VALUE])
@Validated
@Tag(name = "claim", description = "Operations to claim the server")
class ClaimController(private val userLifecycle: TankobonUserLifecycle) {

  @GetMapping
  @Operation(summary = "Get the server claim status")
  fun getClaimStatus() = ClaimStatus(userLifecycle.countUsers() > 0)

  @PostMapping
  @Operation(summary = "Claim the server by creating a new admin user")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "Server was claimed with success",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = UserDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "400",
      description = "Server is already claimed",
      content = [Content()]
    )
  )
  fun claimAdmin(
    @Email(regexp = ".+@.+z\\..+")
    @RequestHeader("X-Tankobon-Email")
    @Parameter(description = "Email of the new admin user to be created")
    email: String,
    @NotBlank
    @RequestHeader("X-Tankobon-Password")
    @Parameter(description = "Password of the new admin user to be created")
    password: String,
  ): UserDto {
    if (userLifecycle.countUsers() > 0) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, "This server has already been claimed")
    }

    return userLifecycle
      .createUser(
        TankobonUser(
          email = email,
          password = password,
          isAdmin = true
        )
      )
      .toDto()
  }

  data class ClaimStatus(
    val isClaimed: Boolean,
  )
}