plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	id("com.google.devtools.ksp")
}

android {
	namespace = "ivandesimone.trustapp"
	compileSdk = 36

	defaultConfig {
		applicationId = "ivandesimone.trustapp"
		minSdk = 29
		targetSdk = 36
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	packaging {
		resources {
			// keep the first copy of the file that causes build error
			pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
		}
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}
}

dependencies {

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.material)
	implementation(libs.androidx.activity)
	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.navigation.fragment.ktx)
	implementation(libs.androidx.navigation.ui.ktx)
	implementation(libs.androidx.fragment)
	implementation(libs.androidx.room.common.jvm)
	implementation(libs.androidx.room.runtime.android)
	implementation(libs.kotlinx.coroutines)
	implementation(libs.gson)
	implementation(libs.retrofit)
	implementation(libs.converter.gson)
	ksp(libs.room.compiler)
	implementation(libs.androidx.recyclerview)
	implementation("com.walletconnect:android-core:1.35.2")
	implementation("com.walletconnect:sign:2.35.2")
	implementation("com.walletconnect:web3modal:1.6.6") // For dApp integration
	implementation(libs.play.services.maps)
	implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}