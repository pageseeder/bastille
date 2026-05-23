plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.codearte.nexus-staging") version "0.30.0"
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
    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("net.sf.ehcache:ehcache:2.10.9.2")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.pageseeder.berlioz:pso-berlioz:0.12.2")
    implementation("org.pageseeder.xmlwriter:pso-xmlwriter:1.0.4")
    implementation("org.pageseeder.cobble:pso-cobble:0.3.2")

    compileOnly("ch.qos.logback:logback-core:1.3.5")
    compileOnly("ch.qos.logback:logback-classic:1.3.5")
    compileOnly("javax.servlet:javax.servlet-api:3.1.0")

    runtimeOnly("net.sf.saxon:Saxon-HE:11.5")
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
