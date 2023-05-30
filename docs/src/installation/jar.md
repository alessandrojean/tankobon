# Run with the Jar file

:::tip
You need Java version 17+ to run Tankobon. Check your version
with `java -version`.
:::

You can run Tankobon from the fat `jar` file. You can download them
in the [releases] section.

In order to run Tankobon, use the following command (replace `x.y.z`
with the actual version number).

```console
$ java -jar tankobon-x.y.z.jar
```

Once Tankobon is started, you can access the [web client].

:::tip
On Windows, you can use `javaw` instead of `java` while running
to launch Tankobon _without_ a command prompt window appearing.
:::

[releases]: #
[web client]: /installation/webclient

## Increase memory limit

By default the `java` process will be limited in the maximum amount of memory
(RAM) it can use, which is usually 1GB. If you encounter some
`OutOfMemoryException` in the logs, you will probably need to increase
the maximum memory Tankobon can use.

To do so, you can use the `-Xmx<limit>` command line flag, where `<limit>`
can be any amount like `2048m`, `4g` etc. For example, to run Tankobon
with a maximum of 4GB of memory:

```console
$ java -jar -Xmx4g tankobon-x.y.z.jar
```

## Updating

To update just stop Tankobon, then start it with the latest `jar`.
