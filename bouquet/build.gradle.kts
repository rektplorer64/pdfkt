plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("kotlin-parcelize")
}

android {
    namespace = "com.rizzi.bouquet"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildTypes {
        debug {
            isJniDebuggable = true
            isMinifyEnabled = false
        }

        release {
            isJniDebuggable = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.2"
    }
}

// Task to bundle the source code into a jar
tasks.register<Jar>("sourceJar") {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.rizzi.composepdf"
                artifactId = "core"
                version = "0.1.0"

                // Make sure the AAR file is included as an artifact
                // artifact("$buildDir/outputs/aar/${project.name}-release.aar")

                // Include the source code artifact
                artifact(tasks["sourceJar"])
            }
        }

        repositories {
            maven {
                name = "jitpack"
                url = uri("https://jitpack.io")
            }
        }
    }
}


dependencies {
    val composeVersion = rootProject.extra["compose_ui_version"]
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("net.engawapg.lib:zoomable:1.6.1")

    implementation("io.coil-kt:coil-compose:2.6.0")
}
