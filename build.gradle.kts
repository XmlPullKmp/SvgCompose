import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

plugins {
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.maven.publish) apply false
}

allprojects {
    plugins.withId(rootProject.libs.plugins.kotlin.compose.get().pluginId) {
        extensions.configure<ComposeCompilerGradlePluginExtension> {
            stabilityConfigurationFiles.addAll(rootProject.layout.projectDirectory.file("stability_config.conf"))
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        // https://docs.gradle.org/8.9/userguide/performance.html#execute_tests_in_parallel
        maxParallelForks = Runtime.getRuntime().availableProcessors()
    }
}
