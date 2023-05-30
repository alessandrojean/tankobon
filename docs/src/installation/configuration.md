# Configuring Tankobon

:::tip
Tankobon has sensible default values for all configuration keys.
You only need to configure it if you want to change the default behaviour.

The `application.yml` file does not exist by default, you need to create
one if you want to customize the configuration.
:::

Tankobon relies heavily on [Spring Boot's configuration], leveraging
`profiles` and configuration `properties`.

The easiest way to configure is either via environment variables
(a good fit for `docker` and `docker-compose`) or by using an
`application.yml` file located in the configuration directory:

- The Docker image will load any `application.yml` file located
  in the `/.tankobon` mounted folder.
- The Jar will load any `application.yml` file loaded in the
  `tankobon.config-dir` directory (defaults to `~/.tankobon`).

Each configuration key can have a different format depending if it's
from the environment variable or from the `application.yml` file.
In the following section both formats will be provided.

You can also specify configuration via the command line, when launching
the Jar. Use the `application-property` form, and prefix with `--`.

```console
$ java -jar tankobon-x.y.z.jar --server.servlet.context-path="/tankobon" --server.port=8443
```

[Spring Boot's configuration]: https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html

## Optional configuration

The following are the optional configuration keys.

### TANKOBON_CONFIGDIR / tankobon.config-dir: `<directory>`

The configuration directory. Will be used to store the logs, database,
and any other file Tankobon needs.

Defaults to `~/.tankobon`. `~` is your home directory on Unix, and your
User profile on Windows.

:::info
When overriding this configuration, you need to use `${user.home}` instead
of `~` (this is a specific Spring Boot variable).
:::

### SERVER_PORT / server.port: `<port>`

Port to listen for the API and web interface.

Defaults to `8080`.

### SERVER_SERVLET_CONTEXT_PATH / server.servlet.context-path: `<baseUrl>`

Base URL, useful if you need to reverse proxy with a subfolder.

Defaults to `/`.

### TANKOBON_REMEMBERME_KEY / tankobon.remember-me.key: `<key>`

If set, the remember-me auto-login feature will be activated, and will
generate a cookie with encoded login information to perform auto-login.
Set `<key>` to any random string.

Not set by default, you need to set it to enable this feature.

### TANKOBON_REMEMBERME_VALIDITY / tankobon.remember-me.validity: `<duration>`

The validity of the generated remember-me cookie. You can specify the
time unit, for example `14d` for 14 days, or `24h` for 24 hours. If
no unit is set, seconds will be assumed and used.

Defaults to `2w` (2 weeks).

### TANKOBON_SESSIONTIMEOUT / tankobon.session-timeout: `<duration>`

The duration after which an inactive session will expire. You can specify the
time unit, for example `14d` for 14 days, or `24h` for 24 hours. If
no unit is set, seconds will be assumed and used.

Defaults to `7d` (7 days).

### TANKOBON_DATABASE_FILE / tankobon.database.file: `<file path>`

File path for the SQLite database.

If you want to change the directory, it is advised to change
`tankobon.config-dir` instead.

Defaults to:

- `\${tankobon.config-dir}/database.sqlite` for Jar.
- `/.tankobon/database.sqlite` for Docker.

### TANKOBON_CORS_ALLOWEDORIGINS / tankobon.cors.allowed-origins: `<origins>`

A list of origins to allow for CORS.

Defaults to empty list.

### LOGGING_FILE_NAME / logging.file.name: `<logfile name>`

Name of the log file.

If you want to change the directory, it is advised to change
`tankobon.config-dir` instead.

Defaults to:

- `\${tankobon.config-dir}/tankobon.log` for Jar.
- `/.tankobon/logs/tankobon.log` for Docker.

:::info
When overriding this configuration, you need to use `${user.home}` instead
of `~` (this is a specific Spring Boot variable).
:::

## Sample configuration file

Here is a sample `application.yml` file in case you need to customize it.
Keep only the lines you need.

::: code-group
```yml [application.yml]
# Only keep lines that are relevant to you.
# Lines starting with # are comments.
# Make sure indentation is correct.
tankobon:
  remember-me:
    # Required to activate the remember-me auto-login via cookies.
    key: changeMePlease
    # Validity of the cookie in seconds, here 30 days.
    validity: 30d
  # Session timeout, here 7 days.
  session-timeout: 7d
  database:
    file: ${user.home}/.tankobon/database.sqlite
  cors.allowed-origins:
    - http://localhost:8081
    - http://localhost:8082
server:
  port: 8080
  servlet.context-path: /tankobon
```
:::


