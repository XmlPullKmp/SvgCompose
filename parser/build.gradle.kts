plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
}

android {
    namespace = "io.github.lemcoder.mikrosoundfont"
    defaultConfig {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

kotlin {
    jvmToolchain(17)

    androidTarget().apply {
        publishLibraryVariants("release")
    }

    iosArm64() // TODO just for removing android APIs while in development

    buildList {
        if (System.getProperty("os.name").lowercase().contains("mac")) {
            add(iosArm64())
            add(iosSimulatorArm64())
            add(iosX64())
            add(macosArm64())
            add(macosX64())
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.android.build.tools)
            implementation(libs.xpp3)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}