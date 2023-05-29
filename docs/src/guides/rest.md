# REST API

Tankobon offers a REST API, which you can browse using Swagger or Redoc.
It's available at `/docs/swagger-ui.html` and `/docs/redoc.html`, respectively.

Under the hoods, Tankobon uses [springdoc-openapi] for semi-automated Open API
specification generation. If you find any issues or inconsistencies with the
API options available while using the Swagger UI or Redoc, please open an
[issue] outlining the problem.

[springdoc-openapi]: https://springdoc.org/
[issue]: https://github.com/alessandrojean/tankobon/issues/new/choose

## Authenticating

Most endpoints requires authentication.

The authentication is done using **Basic Authentication** and can
be set on any endpoint.

## Sessions

Upon successful authentication, a session is created, and can be reused.

- By default, a `SESSION` cookie is set via `Set-Cookie` response header.
  This works well for browsers and clients that can handle cookies.
- If you specify a header `X-Auth-Token` during authentication, the session
  ID will be returned via this same header. You can then pass that header
  again for subsequent requests to reuse the session.

If you need to set the session cookie later on, you can call
`/api/v1/login/set-cookie` with `X-Auth-Token`. The response will contain
the `Set-Cookie` header.

## Logout

You can explicitely logout an existing session by calling
`/api/v1/users/logout`. This would return a HTTP `204`.
