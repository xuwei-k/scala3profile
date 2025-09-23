# scala3profile

[![Latest version](https://index.scala-lang.org/xuwei-k/scala3profile/scala3profile/latest.svg)](https://index.scala-lang.org/xuwei-k/scala3profile/artifacts/scala3profile)

## Setup

### `project/plugins.sbt`

```scala
addSbtPlugin("com.github.xuwei-k" % "sbt-scala3profile" % "version")
```

### `build.sbt`

set `NIGHTLY` Scala 3 version because `scala3profile` is `ResearchPlugin`.

- https://github.com/scala/scala3/blob/2fc299b6c6972733c7e1e46490fee8724acd3b7a/compiler/src/dotty/tools/dotc/plugins/Plugin.scala#L72
- https://github.com/scala/scala3/blob/2fc299b6c6972733c7e1e46490fee8724acd3b7a/docs/_docs/reference/changed-features/compiler-plugins.md?plain=1#L17-L21

You can find latest `NIGHTLY` Scala 3 version here
https://repo.scala-lang.org/ui/packages/gav:%2F%2Forg.scala-lang:scala3-compiler_3
