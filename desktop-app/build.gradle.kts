import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val releaseAppVersion = "1.0.7"
val releaseAppRevision = 1

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.0.1"
}

group = "dolphin.desktop.apps"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation(compose.preview)
    implementation(compose.uiTooling)

    // https://github.com/houbb/opencc4j
    implementation("com.github.houbb:opencc4j:1.7.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb)

            packageName = "DstTranslator"
            packageVersion = releaseAppVersion

            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))

            windows {
                dirChooser = true
                msiPackageVersion = releaseAppVersion
                exePackageVersion = releaseAppVersion
                upgradeUuid = "f33ea1be-e738-43e0-9918-9360b0620fc0"
            }

            linux {
                debMaintainer = "dolphinwing74+github@gmail.com"
                debPackageVersion = releaseAppVersion
                rpmPackageVersion = releaseAppVersion
                appRelease = releaseAppRevision.toString()
            }
        }

        args += listOf("v=$releaseAppVersion")
    }
}
