plugins {
    alias(libs.plugins.convention.android.library.presentation)
}

android {
    namespace = "${NAMESPACE}"
}

dependencies {
    // Add module-specific dependencies here as needed
    ${DEPENDENCIES}
}
