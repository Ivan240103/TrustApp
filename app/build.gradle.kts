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
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = "17"
	}
}

dependencies {
	// General
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.material)
	implementation(libs.androidx.activity)
	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.recyclerview)
	implementation(libs.kotlinx.coroutines)
	// Navigation
	implementation(libs.androidx.navigation.fragment.ktx)
	implementation(libs.androidx.navigation.ui.ktx)
	implementation(libs.androidx.fragment)
	// Room
	implementation(libs.androidx.room.common.jvm)
	implementation(libs.androidx.room.runtime.android)
	ksp(libs.room.compiler)
	// Remote HTTP
	implementation(libs.retrofit)
	implementation(libs.gson)
	implementation(libs.converter.gson)
	// Maps
	implementation(libs.play.services.maps)
	// Charts
	implementation(libs.mpandroidchart)
	// Web3
	implementation(libs.android.core)
	implementation(libs.sign)
	implementation(libs.web3modal)
	implementation(libs.core)
	// Test
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}