import org.gradle.jvm.tasks.Jar

plugins {
    java
}

group = "com.servercontroller"
version = "0.1.0-SNAPSHOT"

subprojects {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.named<Jar>("jar") {
    enabled = false
}
