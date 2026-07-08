plugins {
    id("org.jetbrains.kotlin.jvm") version "2.4.0"
    id("org.jetbrains.intellij.platform") version "2.17.0"
}

group = "com.alexey-anufriev"
version = "1.0.0"

val platformVersion = providers.gradleProperty("platformVersion").getOrElse("2025.3.5")
val resolveCompatibleAiAssistant = providers.gradleProperty("resolveCompatibleAiAssistant").isPresent

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea(platformVersion)
        if (resolveCompatibleAiAssistant) {
            compatiblePlugin("com.intellij.ml.llm")
        }
        else {
            plugin("com.intellij.ml.llm", "253.33514.22")
        }
    }
}
