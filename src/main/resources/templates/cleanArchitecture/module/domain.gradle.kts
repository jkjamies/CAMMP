plugins {
    alias(libs.plugins.convention.android.library.domain)
}

android {
    namespace = "${NAMESPACE}"
}

dependencies {
    // Add module-specific dependencies here as needed
    ${DEPENDENCIES}
}
