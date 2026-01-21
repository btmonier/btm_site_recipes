plugins {
    kotlin("multiplatform") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
}

group = "org.btmonier"
version = "0.1.1"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
    
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
                devServer = devServer?.copy(open = true) ?: org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer(open = true)
            }
            binaries.executable()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.11.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.9.0")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("com.charleskorn.kaml:kaml:0.60.0")
            }
        }
    }
}

// Task to convert markdown recipes to JSON
tasks.register<JavaExec>("convertRecipesToJson") {
    group = "recipes"
    description = "Convert markdown recipe files to JSON"
    classpath = sourceSets["jvmMain"].runtimeClasspath
    mainClass.set("org.btmonier.recipes.RecipeToJsonConverterKt")
    
    // Use the same Java toolchain as compilation (Java 21)
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    })
    
    // Default arguments - can be overridden with -PinputDir and -PoutputFile
    val inputDir = project.findProperty("inputDir") as String? ?: "recipes_md"
    val outputFile = project.findProperty("outputFile") as String? ?: "src/jsMain/resources/content/recipes.json"
    
    args(inputDir, outputFile)
}
