plugins {
    `java-library`
}

group = "com.servercontroller"
version = "0.1.0-SNAPSHOT"

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    api("com.fasterxml.jackson.core:jackson-annotations:2.17.2")
    api("com.fasterxml.jackson.core:jackson-core:2.17.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
