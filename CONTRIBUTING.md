# Contributing to CAMMP

Thank you for your interest in contributing to **CAMMP (Clean Architecture Multi-Module Plugin)**! We appreciate your help in making Android development architecturally sound and efficient.

## 🤝 How to Contribute

### Reporting Bugs
If you find a bug, please create a GitHub issue with:
1.  **Description**: What happened?
2.  **Reproduction Steps**: How can we make it happen again?
3.  **Expected Behavior**: What should have happened?
4.  **Environment**: OS, IntelliJ/Android Studio version, Plugin version.

### Submitting Pull Requests
1.  **Fork** the repository and clone it locally.
2.  Create a branch for your feature or fix (`git checkout -b feature/amazing-feature`).
3.  Commit your changes following the [Coding Standards](#-coding-standards).
4.  Push to your fork and submit a **Pull Request**.

## 📏 Coding Standards

We follow strict architectural and stylistic guidelines to maintain the "Architectural Guardian" vision.

### Architecture
-   **Clean Architecture**: Respect the boundaries. `:domain` depends on nothing. `:data` depends on `:domain`.
-   **MVI**: All presentation logic must follow Model-View-Intent.
    -   Immutable `UiState`.
    -   Single source of truth (ViewModel).
    -   One-off events (SideEffects) via Channels.

### Kotlin Style
-   **Immutability**: `val` is king. Mutable lists are forbidden in public APIs.
-   **Exhaustiveness**: Use `when` as an expression or statement to handle all cases of sealed classes.
-   **Testing**:
    -   Write **Kotest** specifications for business logic.
    -   Use **Fakes** over Mocks for data repositories.
    -   Do **not** mock data classes.

### Hygiene
-   Ensure all new files have the **Apache 2.0 Copyright Header**.
-   Run `./gradlew check` before submitting to ensure tests and linting pass.

## 🛠 Building the Project

```bash
# Run tests
./gradlew test

# Run the plugin in a sandboxed IDE
./gradlew runIde
```

Thank you for helping us build the ultimate architectural tool! 🚀
