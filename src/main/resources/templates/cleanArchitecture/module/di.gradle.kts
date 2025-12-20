plugins {
    alias(libs.plugins.convention.android.library.di)
}

android {
    namespace = "${NAMESPACE}"
}

dependencies {
    // Add module-specific dependencies here as needed
    ${DEPENDENCIES}
}
