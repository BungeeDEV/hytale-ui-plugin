plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij.platform") version "2.10.5"
}

val pluginVersion = "1.3.0"

group = "de.bungee.idea.plugins.uifile"
version = pluginVersion
// Configure Java compatibility for JDK 21 (required by IntelliJ Platform 2025.1+)
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

// Optimize Java compilation
tasks.withType<JavaCompile> {
    options.apply {
        encoding = "UTF-8"
        // Enable all compiler optimizations
        compilerArgs.addAll(
            listOf(
                "-Xlint:all",
                "-Xlint:-serial",
                "-parameters"
            )
        )
        // Enable incremental compilation
        isIncremental = true
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}
// Exclude Kotlin stdlib to avoid conflicts with IntelliJ Platform's bundled version
configurations.all {
    exclude(
        group = "org.jetbrains.kotlin",
        module = "kotlin-stdlib"
    )
    exclude(
        group = "org.jetbrains.kotlin",
        module = "kotlin-stdlib-common"
    )
    exclude(
        group = "org.jetbrains.kotlin",
        module = "kotlin-stdlib-jdk8"
    )
    exclude(
        group = "org.jetbrains.kotlin",
        module = "kotlin-stdlib-jdk7"
    )
}
dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.1")
        bundledPlugins(listOf(/* Plugin Dependencies */))
    }
    testImplementation("junit:junit:4.13.2")
}
// Configure Gradle IntelliJ Platform Plugin
intellijPlatform {
    buildSearchableOptions = false
    pluginConfiguration {
        version = pluginVersion
        ideaVersion {
            sinceBuild = "251"
            untilBuild = "253.*"
        }
    }
    pluginVerification {
        ides {
            recommended()
        }
    }
    signing {
        certificateChain =
            System.getenv("CERTIFICATE_CHAIN")
        privateKey = System.getenv("PRIVATE_KEY")
        password = System.getenv("PRIVATE_KEY_PASSWORD")
    }
    publishing {
        token = System.getenv("PUBLISH_TOKEN")
    }
}
