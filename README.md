# scala3profile

[![Latest version](https://index.scala-lang.org/xuwei-k/scala3profile/scala3profile/latest.svg)](https://index.scala-lang.org/xuwei-k/scala3profile/artifacts/scala3profile)

## Setup

### `project/plugins.sbt`

```scala
addSbtPlugin("com.github.xuwei-k" % "sbt-scala3profile" % "version")
```

### `build.sbt`

set `NIGHTLY` Scala 3 version because `scala3profile` is `ResearchPlugin`.

- https://github.com/lampepfl/dotty/blob/ab2ed3199d399d/compiler/src/dotty/tools/dotc/plugins/Plugin.scala#L55
- https://github.com/lampepfl/dotty/blob/ab2ed3199d399d/docs/_docs/reference/changed-features/compiler-plugins.md?plain=1#L17-L21

You can find latest `NIGHTLY` Scala 3 version here
https://repo1.maven.org/maven2/org/scala-lang/scala3-compiler_3/
