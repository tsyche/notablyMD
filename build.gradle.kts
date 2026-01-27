buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10")
        classpath("org.apache.commons:commons-configuration2:2.4")
    }
}

plugins {
    id("com.google.devtools.ksp") version "2.1.10-1.0.30" apply false
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory.asFile)
}