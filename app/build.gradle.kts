import org.gradle.jvm.tasks.Jar

plugins {
    application
    java
}

group = "com.servercontroller"
version = "0.1.0-SNAPSHOT"

val packagingVersion = "0.1.0"

dependencies {
    implementation(project(":common"))
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.github.javakeyring:java-keyring:1.0.4")

    val javafxVersion = "21.0.4"
    val javafxPlatform = org.gradle.internal.os.OperatingSystem.current().run {
        when {
            isWindows -> "win"
            isMacOsX -> "mac"
            else -> "linux"
        }
    }

    implementation("org.openjfx:javafx-base:$javafxVersion:$javafxPlatform")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:$javafxPlatform")
    implementation("org.openjfx:javafx-controls:$javafxVersion:$javafxPlatform")
    implementation("org.openjfx:javafx-fxml:$javafxVersion:$javafxPlatform")
}

application {
    mainClass.set("com.servercontroller.app.ServerControllerLauncher")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes("Main-Class" to application.mainClass.get())
    }
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })
}

repositories {
    mavenCentral()
}

val jpackageInputDir = layout.buildDirectory.dir("jpackage-input")
val jpackageOutputDir = layout.buildDirectory.dir("jpackage")

tasks.register<Copy>("prepareJpackage") {
    dependsOn(tasks.jar)
    from(tasks.jar)
    from(configurations.runtimeClasspath)
    into(jpackageInputDir)
    outputs.upToDateWhen { false }
}

tasks.register<Exec>("jpackageAppImage") {
    dependsOn("prepareJpackage")
    val osName = System.getProperty("os.name").lowercase()
    val jpackageName = if (osName.contains("win")) "jpackage.exe" else "jpackage"
    val javaLauncher = javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    val jpackageExecutable = javaLauncher.map { launcher ->
        launcher.metadata.installationPath.file("bin/$jpackageName").asFile.absolutePath
    }
    val mainJarName = tasks.jar.get().archiveFile.get().asFile.name

    doFirst {
        delete(jpackageOutputDir)
        jpackageOutputDir.get().asFile.mkdirs()
    }

    commandLine(
        jpackageExecutable.get(),
        "--type", "app-image",
        "--dest", jpackageOutputDir.get().asFile.absolutePath,
        "--input", jpackageInputDir.get().asFile.absolutePath,
        "--name", "twpUI",
        "--main-jar", mainJarName,
        "--main-class", application.mainClass.get(),
        "--app-version", packagingVersion
    )
}

tasks.register<Exec>("jpackageInstaller") {
    dependsOn("prepareJpackage")
    val osName = System.getProperty("os.name").lowercase()
    val jpackageName = if (osName.contains("win")) "jpackage.exe" else "jpackage"
    val javaLauncher = javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    val jpackageExecutable = javaLauncher.map { launcher ->
        launcher.metadata.installationPath.file("bin/$jpackageName").asFile.absolutePath
    }
    val mainJarName = tasks.jar.get().archiveFile.get().asFile.name
    val packageType = if (osName.contains("win")) "msi" else "app-image"

    doFirst {
        delete(jpackageOutputDir)
        jpackageOutputDir.get().asFile.mkdirs()
    }

    val args = mutableListOf(
        jpackageExecutable.get(),
        "--type", packageType,
        "--dest", jpackageOutputDir.get().asFile.absolutePath,
        "--input", jpackageInputDir.get().asFile.absolutePath,
        "--name", "twpUI",
        "--main-jar", mainJarName,
        "--main-class", application.mainClass.get(),
        "--app-version", packagingVersion
    )

    if (osName.contains("win")) {
        args.addAll(listOf("--win-menu", "--win-shortcut"))
    }

    commandLine(args)
}
