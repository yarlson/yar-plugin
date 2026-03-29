import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
    id("org.jetbrains.intellij.platform") version "2.13.1"
    id("org.jetbrains.grammarkit") version "2023.3.0.3"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(25)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

sourceSets {
    main {
        java.srcDirs("src/main/gen")
    }
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
    }
    pluginVerification {
        ides {
            recommended()
        }
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(providers.gradleProperty("platformVersion"))
        pluginVerifier()
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
}


tasks {
    val generateLexer by getting
    val generateParser by getting

    compileKotlin {
        dependsOn(generateLexer, generateParser)
    }
    compileJava {
        dependsOn(generateLexer, generateParser)
    }

    generateLexer {
        sourceFile.set(file("src/main/grammars/Yar.flex"))
        targetOutputDir.set(file("src/main/gen/dev/yarlson/yar/lexer"))
    }

    generateParser {
        sourceFile.set(file("src/main/grammars/Yar.bnf"))
        targetRootOutputDir.set(file("src/main/gen"))
        pathToParser.set("/dev/yarlson/yar/parser/YarParser.java")
        pathToPsiRoot.set("/dev/yarlson/yar/psi")
    }

    clean {
        delete("src/main/gen")
    }
}
