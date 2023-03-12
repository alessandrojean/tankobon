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
      <html lang="en-US">
        <head>
          <title>Tankobon API documentation</title>
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1" />
          <link rel="preconnect" href="https://rsms.me/">
          <link rel="preconnect" href="https://cdn.redoc.ly/">
          <link rel="preconnect" href="https://fonts.googleapis.com">
          <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
          <link rel="stylesheet" href="https://rsms.me/inter/inter.css">
          <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:ital,wght@0,400;0,500;0,700;1,400;1,500;1,700&display=swap" rel="stylesheet">
          
          <style>
            body {
              margin: 0;
              padding: 0;
            }
            
            @font-face {
              font-family: "Inter Rounded";
              font-style: normal;
              font-weight: 400;
              font-display: swap;
              font-feature-settings: "cv02", "cv03", "cv04", "cv11";
              src: url("https://rsms.me/inter/font-files/Inter-Regular.woff2?v=3.19") format("woff2");
            }
            
            @font-face {
              font-family: "Inter Rounded";
              font-style: italic;
              font-weight: 400;
              font-display: swap;
              font-feature-settings: "cv02", "cv03", "cv04", "cv11";
              src: url("https://rsms.me/inter/font-files/Inter-Italic.woff2?v=3.19") format("woff2");
            }
            
            @font-face {
              font-family: "Inter Rounded";
              font-style: normal;
              font-weight: 700;
              font-display: swap;
              font-feature-settings: "cv02", "cv03", "cv04", "cv11";
              src: url("https://rsms.me/inter/font-files/Inter-Bold.woff2?v=3.19") format("woff2");
            }
            
            @font-face {
              font-family: "Inter Rounded";
              font-style: italic;
              font-weight: 700;
              font-display: swap;
              font-feature-settings: "cv02", "cv03", "cv04", "cv11";
              src: url("https://rsms.me/inter/font-files/Inter-BoldItalic.woff2?v=3.19") format("woff2");
            }
          </style>
        </head>
        <body>
          <div id="redoc-container"></div>
          <script src="https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js"></script>
          <script>
            const interFont = `'Inter', ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica Neue, Arial, Noto Sans, sans-serif`;
            const interRoundedFont = `'Inter Rounded', ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica Neue, Arial, Noto Sans, sans-serif`;
            const jetbrainsMonoFont = `'JetBrains Mono', ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, Liberation Mono, Courier New, monospace`;
            const redocOptions = {
              sortOperationsAlphabetically: true,
              sidebarLinks: {
                beforeInfo: [
                  { label: 'GitHub', link: 'https://github.com/alessandrojean/tankobon', target: '_blank' },
                ],
              },
              theme: {
                typography: {
                  fontFamily: interFont,
                  fontSize: '14px',
                  code: {
                    fontFamily: jetbrainsMonoFont,
                    fontWeight: 500,
                  },
                  headings: {
                    fontFamily: interRoundedFont,
                    fontWeight: 700,
                  },
                  rightPanelHeading: { fontFamily: interFont },
                },
              },
            };
            const redocContainer = document.getElementById('redoc-container');
            Redoc.init('/api/api-docs', redocOptions, redocContainer);
          </script>
        </body>
      </html>
    """.trimIndent()
}