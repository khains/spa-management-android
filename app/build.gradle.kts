import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Doc thong tin keystore tu file keystore.properties (khong commit file nay len git).
// Neu chua tao file, build release se bao loi ro rang thay vi loi kho hieu.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
val hasKeystoreProperties = keystorePropertiesFile.exists()
if (hasKeystoreProperties) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.spa.management"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.spa.management"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Doi thanh dia chi IP/domain cua backend that khi build
        // Vi du chay local: "http://10.0.2.2:5000/" (10.0.2.2 la localhost tren Android emulator)
        buildConfigField("String", "BASE_URL", "\"https://spa-management-backend.onrender.com/\"")

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    signingConfigs {
        if (hasKeystoreProperties) {
            create("release") {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            // Doi BASE_URL sang domain backend that (https) khi phat hanh chinh thuc
            buildConfigField("String", "BASE_URL", "\"https://spa-management-backend.onrender.com/\"")

            // Tam de false de tranh R8 lam vo Gson (parse JSON qua reflection).
            // Neu muon bat minify, can bo sung day du quy tac -keep cho cac data class trong proguard-rules.pro
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            if (hasKeystoreProperties) {
                signingConfig = signingConfigs.getByName("release")
            }
            // Neu chua co keystore.properties: Gradle se KHONG tu dong ky APK release.
            // File tao ra se la app-release-unsigned.apk va KHONG cai truc tiep len may duoc,
            // can tao keystore.properties (xem huong dan trong README) roi build lai.
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // QR scan cho check-in (tuy chon, dung ZXing)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // DataStore de luu token dang nhap
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.5")
}