plugins {
    java
    `java-library`
}

group = "com.servercontroller"
version = "0.1.0-SNAPSHOT"

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    implementation(project(":common"))
    implementation("io.netty:netty-all:4.1.111.Final")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("servercontroller")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand(mapOf("version" to project.version))
        }
    }
}
