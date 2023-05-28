# Tankobon

Tankobon is a free and open source physical book collection manager,
created with [Kotlin], [Spring Boot] and [Vue.js].

<p align="center">
  <picture style="width: 90%">
    <source media="(prefers-color-scheme: dark)" srcset="./.github/images/book-demo-img-dark.png">
    <source media="(prefers-color-scheme: light)" srcset="./.github/images/book-demo-img-light.png">
    <img alt="Screenshot of Tankobon" src="./.github/images/book-demo-img-dark.png">
  </picture>
</p>

> **Warning**
> Tankobon is under active development and is an ongoing **WIP**.
> Anyone is welcome to try it out, but do not expect a fully featured,
> bug-free experience. Some features will be missing and/or broken.
> It's not recommended to be used in production yet. Stay tuned
> for any news and future stable releases.

[Kotlin]: https://kotlinlang.org/
[Spring Boot]: https://spring.io/
[Vue.js]: https://vuejs.org/

## Features

The following items are the major features aimed to be added to
Tankobon on the initial releases.

- Generic book support, including manga and comics;
- Import books by ISBN from Open Library, Google Books™ and others;
- Publishers, people, stores, groups and other related entities;
- Multiple users, shared libraries and administration tools;
- Cover search by ISBN, code or book title;
- Book readings history tracking;
- Search with advanced query syntax;
- Upload custom covers and pictures;
- Monthly statistics of expense.

The project is open to suggestions and ideas, so feel free
to reach out if you have anything you'd like to see.

## Download

Get the tool from our [releases page] or through [Docker].

[releases page]: https://github.com/alessandrojean/tankobon
[Docker]: #running-through-docker

## Getting started

Tankobon isn't ready for normal usage yet. For now, you can follow the
[Contributing] section to build from source and run locally or try a
nightly Docker image build.

[Contributing]: #contributing

### Running through Docker

The nightly images are available in [Docker Hub] and [GitHub Packages].
You can run manually by using the `docker` command or by using [Docker Compose].

<details>
  <summary>Command-line instructions</summary>

  1. Pull the Docker image.

     ```console
     $ docker pull alessandrojean/tankobon:nightly
     ```

     If you want to use the image from [GitHub Packages], use the command below instead.

     ```console
     $ docker pull ghcr.io/alessandrojean/tankobon:nightly
     ```
  2. Start a Docker container in detached mode.

     ```
     $ docker run -d \
         -p 25565:8080 \
         -v /path/to/user_home/.tankobon:/root/.tankobon \
         alessandrojean/tankobon:nightly
     ```
  3. Open http://localhost:25565 on a browser and proceed with the claim setup
     to create the first administrator user.
</details>

<details>
  <summary>Docker Compose instructions</summary>

  1. Create a `docker-compose.yml` file.

     ```yaml
     version: '3.9'
     services:
       tankobon:
         # To use the GitHub Packages image, use the line below instead.
         # image: ghcr.io/alessandrojean/tankobon:nightly
         image: alessandrojean/tankobon:nightly
         ports:
           # Tankobon will be available at port 25565.
           - '25565:8080'
         volumes:
           # The app files will be available outside the container.
           - /path/to/user_home/.tankobon:/root/.tankobon
     ```
  2. Start a Docker container in detached mode.

     ```console
     $ docker-compose up -d
     ```
  3. Open http://localhost:25565 on a browser and proceed with the claim setup
     to create the first administrator user.
</details>

[Docker Hub]: https://hub.docker.com/r/alessandrojean/tankobon
[GitHub Packages]: https://github.com/alessandrojean/tankobon/pkgs/container/tankobon
[Docker Compose]: https://docs.docker.com/compose/

## Contributing

Contributions are very **welcome**! Please review the [CONTRIBUTING.md]
guide before getting started.

<details>
  <summary>Development instructions</summary>

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
  and follow the claim setup at the first time to create the administrator user.

  If you use IntelliJ Idea, you can use some run configurations provided with
  the project that will make easier to run the application within specific
  contexts such as `localdb`, `noclaim` and `dev`.
</details>

[pnpm]: https://pnpm.io/
[Node.js]: https://nodejs.org/
[JDK 17]: https://openjdk.org/projects/jdk/17/
[CONTRIBUTING.md]: CONTRIBUTING.md

## Documentation

Check the documentation at the website (to be written).

### API

For the API there is a OpenAPI V3 documentation included with the server. It can be accessed through `/docs/swagger-ui.html` for a graphical experience, or through `/docs/openapi-v3` or `/docs/openapi-v3.yaml` to get the Tankobon OpenAPI V3 specification in a raw format.

## Project structure

Tankobon has a monorepo structure.

### `/server`

The core Spring Boot backend that powers up Tankobon.

### `/client`

The standard Vue.js web client that consumes the API.

## Acknowledgements

[Komga] project structure is the main inspiration for the Tankobon
code structure. Although it's a self-hosted tool with a different
purpose, it's definetely worth taking a look into it.

[Komga]: https://github.com/gotson/komga/

## License

> You can check out the full license [here](LICENSE).

This repository is licensed under the terms of the **MIT** license.

