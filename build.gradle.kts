plugins {
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.nexus.staging)
}

val title: String by project

group       = "org.pageseeder.bastille"
version     = file("version.txt").readText().trim()
description = title

apply(from = "gradle/publish-mavencentral.gradle.kts")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

repositories {
    maven {
        url = uri("https://maven-central.storage.googleapis.com/maven2")
    }
    maven {
        url = uri("https://s01.oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    implementation(libs.slf4j.api)
    implementation(libs.ehcache)
    implementation(libs.commons.io)
    implementation(libs.berlioz)
    implementation(libs.xmlwriter)
    implementation(libs.cobble)

    compileOnly(libs.logback.core)
    compileOnly(libs.logback.classic)
    compileOnly(libs.servlet.api)

    runtimeOnly(libs.saxon)
}

tasks.test {
    useJUnitPlatform()
}

nexusStaging {
    packageGroup = project.group.toString()
    stagingProfileId = findProperty("sonatypeStagingProfileId") as String?
    serverUrl = "https://s01.oss.sonatype.org/service/local/"
    username = findProperty("sonatypeUsername") as String?
    password = findProperty("sonatypePassword") as String?
}
