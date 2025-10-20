plugins {
    java
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("app.Main")
}

repositories {
    mavenCentral()
    // JitPack для библиотек demidko
    maven("https://jitpack.io")
}

dependencies {
    // библиотека словаря AOT (lookupForMeanings)
    implementation("com.github.demidko:aot:2021.09.19")

    // дополнительные компоненты, как в твоем Gradle-примере
    implementation("com.github.demidko:bits:2022.08.06")
    implementation("com.github.demidko:aot-bytecode:2025.02.15")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
