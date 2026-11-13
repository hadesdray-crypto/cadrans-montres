plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// La cle de signature reste HORS du depot. Variables : CADRANS_KEYSTORE, CADRANS_STOREPASS, CADRANS_KEYALIAS, CADRANS_KEYPASS.
val cleChemin: String? = System.getenv("CADRANS_KEYSTORE")
val cleDispo = cleChemin != null && file(cleChemin).exists()

android {
    namespace = "fr.cadrans"
    compileSdk = 35

    defaultConfig {
        applicationId = "fr.cadrans.studio"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (cleDispo) {
            create("cadrans") {
                storeFile = file(cleChemin!!)
                storePassword = System.getenv("CADRANS_STOREPASS")
                keyAlias = System.getenv("CADRANS_KEYALIAS")
                keyPassword = System.getenv("CADRANS_KEYPASS")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            if (cleDispo) signingConfig = signingConfigs.getByName("cadrans")
        }
        getByName("release") {
            isMinifyEnabled = false
            if (cleDispo) signingConfig = signingConfigs.getByName("cadrans")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}