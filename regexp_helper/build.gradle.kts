plugins {
  id("java")
  id("org.jetbrains.intellij") version "1.5.2"
}

group = "com.intellij.sdk"
version = "0.1.0"

repositories {
  mavenCentral()
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
  version.set("2021.2.4")
  type.set("PY")
  plugins.set(listOf("Pythonid"))
  downloadSources.set(false)
}

tasks {
  buildSearchableOptions {
    enabled = false
  }

  patchPluginXml {
    version.set("${project.version}")
    sinceBuild.set("212")
    untilBuild.set("221.*")
  }
}
