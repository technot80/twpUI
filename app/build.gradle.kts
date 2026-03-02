plugins {
    application
    java
}

group = "com.servercontroller"
version = "0.1.0-SNAPSHOT"

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
    mainClass.set("com.servercontroller.app.ServerControllerApp")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}
