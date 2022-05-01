plugins {
    java
}

group = "org.csc.java"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

allprojects {
    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8";
        }
        withType<Test> {
            useJUnitPlatform()
        }
    }
}