plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij.platform") version "2.10.5"
}

val pluginVersion = "1.2.4"

group = "de.bungee.idea.plugins.uifile"
version = pluginVersion
// Configure Java compatibility for JDK 17 (required by IntelliJ Platform 2025.1+)
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
kotlin {
    jvmToolchain(17)
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
