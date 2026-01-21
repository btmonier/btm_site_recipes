# Recipes

Basic recipe website built using [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)

- **Static website and interface**: Kotlin/JS and `kotlinx-html`
- **Markdown recipe parser to JSON**: Kotlin/JVM and `kotlinx-serialization`

## Prerequisites

- JDK 21+

## Actions

Build the project:
```bash
./gradlew build
```

Convert markdown recipes to JSON:
```bash
./gradlew convertRecipesToJson
```

Run the development server locally:
```bash
./gradlew jsBrowserDevelopmentRun
```

## Adding Recipes

Add new `.md` files to the `recipes_md/` directory following the existing format.
