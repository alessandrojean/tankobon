# Contributing

If you're interested in supporting Tankobon's development, and you know
how to code, follow the steps outlined in the [developer guide]
for information about setting up your development environment.

[developer guide]: #developer-guide

## Developer guide

Contributions are encouraged and welcome! Be sure to review the
[CONTRIBUTING.md] before getting started with your contribution.

### Setup the development environment

Here are some basic instructions on how you can start developing Tankobon.

1. Install [pnpm], [Node.js] and [JDK 17].
2. Run the server:

  ```console
  $ ./gradlew bootRun --args='--spring.profiles.active=dev,localdb'
  ```
3. Run the client:

  ```console
  $ pnpm dev
  ```

And that's it! Open [http://localhost:8081](http://localhost:8081) in a browser
and follow the [Accessing the web client] guide to claim the server and
create your first library.

### IDEs and tools recommended

We recommend using [IntelliJ IDEA] for the server development and [Visual
Studio Code] for the client development. Both projects are configured
to work better with these tools, although you're free to use any other
that supports Kotlin, TypeScript and Vue.js.

If you use IntelliJ IDEA, you can use some run configurations provided with
the project that will make easier to run the server within specific
Spring profiles such as `localdb`, `noclaim` and `dev`.

[CONTRIBUTING.md]: https://github.com/alessandrojean/tankobon/blob/main/CONTRIBUTING.md
[pnpm]: https://pnpm.io/
[Node.js]: https://nodejs.org/
[JDK 17]: https://openjdk.org/projects/jdk/17/
[Accessing the web client]: /guides/webclient
[IntelliJ IDEA]: https://www.jetbrains.com/idea/
[Visual Studio Code]: https://code.visualstudio.com/

## Notes and tips

Tankobon code architecture ended up being large and complex. Here are
some few notes and tips to help you get started.

### Where to start?

If you aren't sure what to work on, it's recommended taking a look at
some of the open [issues] on GitHub. Make sure the issue you're interested
in contributing isn't already being worked by checking the [project] as well.

In general, some good places to start might be:

- Writing missing documentation or fixing grammar issues on it.
- Implementing missing tests for the server and client.
- Designing and/or implementing UI elements.
- Docker build optimizations and caching strategies.
- CI pipelines and workflows.

[issues]: https://github.com/alessandrojean/tankobon/issues
[project]: https://github.com/users/alessandrojean/projects/1

### Spring profiles

Tankobon uses Spring Profiles extensively.

`dev`
: Add more logging, in-memory database, and enable CORS from `localhost:8081`.

`localdb`
: A `dev` profile that stores the database in `./localdb`.

`demo`
: Limit some actions that can be done by all users. Useful for demo servers.

`noclaim`
: Will create initial users at startup if none exist and output users
  and passwords in the standard output.

  - If `dev` is active, will create `admin@example.org` with password
    `admin`, and `user@example.org` with password `user`.
  - If `dev` is not active, will create `admin@example.org` with a
    random password that will be shown in the logs.

### Code style, formatting, and linting

Tankobon is developed using [Kotlin] and [TypeScript].

For the server code in Kotlin, [ktlint] is used for formatting and linting.
For TypeScript, [ESLint] with [@antfu/eslint-config] is used for formatting
and linting.

The repository uses 2 spaces for indentation.

#### Formatting and linting the server code

To run `ktlint` in the server code, you can use the following command.

```console
$ ./gradlew ktlintFormat
```

Alternatively you can use the `Run [ktlintFormat]` run configuration
in IntelliJ IDEA.

#### Formatting and linting the client code

To run ESLint in the client code, you can use the following command.

```console
$ pnpm lint:fix
```

[Kotlin]: https://kotlinlang.org/
[TypeScript]: https://www.typescriptlang.org/
[ktlint]: https://pinterest.github.io/ktlint
[ESLint]: https://eslint.org/
[@antfu/eslint-config]: https://github.com/antfu/eslint-config

### Commits

The repository follows the [Conventional Commits] specification to easily
create better changelogs. To make sure you're following the specification,
you can use tools like [Commitizen] while commiting.

Although it's not mandatory to follow, using is recommended and it will
help your contribution to be more explicit about what it's doing and
what it's addressing.

[Conventional Commits]: https://www.conventionalcommits.org/
[Commitizen]: https://github.com/commitizen/cz-cli

### Developer resources

Below is a list of resources that are useful for the Tankobon development.

- [Kotlin website](https://kotlinlang.org/)
- [Spring Boot website](https://spring.io/projects/spring-boot/)
- [Vue.js website](https://vuejs.org/)
- [Tailwind CSS](https://tailwindcss.com/)
- [Headless UI](https://headlessui.com/)

## Translation

Tankobon is a work in progress and is subject to a lot of changes yet.
For that reason, we don't recommend translating the resources to other
languages yet, as they will be outdated pretty quickly.

## Financial contributions

If you'd like to support the project financially, you can do so
using any of the following platforms.

- [GitHub Sponsors](https://github.com/sponsors/alessandrojean)
- [Ko-fi](https://ko-fi.com/alessandrojean)
