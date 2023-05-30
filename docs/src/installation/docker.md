# Run with Docker

## Registries

The Docker images are published on:

- [DockerHub]
- [GitHub Packages]

[DockerHub]: https://hub.docker.com/r/alessandrojean/tankobon
[GitHub Packages]: https://github.com/users/alessandrojean/packages/container/package/tankobon

## Version tags

This image provides various versions that are available via tags.

| Tag       | Description     |
| --------- | --------------- |
| `nightly` | latest commit   |
| `x.y.z`   | version `x.y.z` |
| `hash`    | specific commit |

## Usage

Here are some example snippets to help you get started creating a container.

### Using `docker`

```console
$ docker create \
    --name=tankobon \
    -p 25565:8080 \
    -v /path/to/user_home/.tankobon:/root/.tankobon \
    --restart unless-stopped \
    alessandrojean/tankobon:nightly
```

Then start the container:

```console
$ docker start tankobon -d
```

### Using `docker-compose`

```yml
version: '3.9'
services:
  tankobon:
    image: alessandrojean/tankobon:nightly
    container_name: tankobon
    ports:
      # Tankobon will be available at port 25565.
      - '25565:8080'
    volumes:
      # The app files will be available outside the container.
      - /path/to/user_home/.tankobon:/root/.tankobon
    restart: unless-stopped
```

Then start the container:

```console
$ docker-compose up -d
```

## Parameters

Container images are configured using parameters passed at runtime (such
as those above). These parameters are separated by a colon and indicate
`external:internal` respectively. For example, `-p 25565:8080` would expose
port `8080` from inside the container to be accessible from the host's IP
on port `25565` outside the container.

| Parameter | Function |
| :-------- | :------- |
| `-p 25565:8080` | The port for the Tankobon APIs and web interface |
| `-v /path/to/user_home/.tankobon:/root/.tankobon` | Database and Tankobon configurations |
| `-e ENV_VAR=value ` | Any configuration environment variable |

## Increase memory limit

By default the `java` process will be limited in the maximum amount of memory
(RAM) it can use, which is usually 1GB. If you encounter some
`OutOfMemoryException` in the logs, you will probably need to increase
the maximum memory Tankobon can use.

To do so, you can use the `JAVA_TOOL_OPTIONS=-Xmx<limit>` environment
variable, where `<limit>` can be any amount like `2048m`, `4g` etc.
For example, to run Tankobon with a maximum of 4GB of memory:

```bash
JAVA_TOOL_OPTIONS=-Xmx4g
```

## Support information

- Shell access whilst the container is running:

  ```console
  $ docker exec -it tankobon /bin/bash
  ```
- To monitor the logs of the container in realtime:

  ```console
  $ docker logs -f tankobon
  ```

## Updating

Below are the instructions for updating containers.

### Via `docker run`/`docker create`

1. Update the image.

   ```console
   $ docker pull alessandrojean/tankobon:nightly
   ```
2. Stop the running container.

   ```console
   $ docker stop tankobon
   ```
3. Delete the container.

   ```console
   $ docker rm tankobon
   ```
4. Recreate a new container with the same `docker create` parameters
   as instructed above (if mapped correctly to a host folder, your
   `.tankobon` folder and settings will be preserved).
5. Start the new container.

   ```console
   $ docker start tankobon -d
   ```
6. You can also remove the old dangling images.

   ```console
   $ docker image prune
   ```

### Via `docker-compose`

1. Update all images.

   ```console
   $ docker-compose pull
   ```

   Alternatively update a single image.

   ```console
   $ docker-compose pull tankobon
   ```
2. Let `docker-compose` update all containers as necessary.

   ```console
   $ docker-compose up -d
   ```

   Alternatively update a single container.

   ```console
   $ docker-compose up -d tankobon
   ```
3. You can also remove the old dangling images.

   ```console
   $ docker image prune
   ```

### Automatic updates

You can use [Watchtower] to automatically update your containers.

[Watchtower]: https://github.com/containrrr/watchtower
