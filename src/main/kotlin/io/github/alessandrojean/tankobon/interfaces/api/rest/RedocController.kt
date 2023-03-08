package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
@Hidden
class RedocController {

  @GetMapping(produces = [MediaType.TEXT_HTML_VALUE])
  @ResponseBody
  fun redocDocumentation(): String =
    """
      <!DOCTYPE html>
      <html>
        <head>
          <title>Tankobon API documentation</title>
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <meta href="https://fonts.googleapis.com/css?family=Montserrat:300,400,700|Roboto:300,400,700" rel="stylesheet">
          
          <style>
            body {
              margin: 0;
              padding: 0;
            }
          </style>
        </head>
        <body>
          <redoc spec-url="/api/api-docs"></redoc>
          <script src="https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js"></script>
        </body>
      </html>
    """.trimIndent()
}