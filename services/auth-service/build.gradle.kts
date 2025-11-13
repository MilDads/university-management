plugins {
    id("java")
    id("application")
}

group = "io.github.bardiakz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "io.github.bardiakz.Main"
}

tasks.test {
    useJUnitPlatform()
}