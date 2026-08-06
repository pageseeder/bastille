plugins {
    `java-library`
    `maven-publish`
    jacoco
    alias(libs.plugins.cyclonedx)
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.versions)
}

val title: String = project.property("title") as String
val website: String = project.property("website") as String
val gitName: String = project.property("gitName") as String

group       = "org.pageseeder.bastille"
version     = file("version.txt").readText().trim()
description = title

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named<Javadoc>("javadoc") {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
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

    compileOnly(libs.jspecify)
    compileOnly(libs.logback.core)
    compileOnly(libs.logback.classic)
    compileOnly(libs.servlet.api)

    runtimeOnly(libs.saxon)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.servlet.api)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyLocking {
    lockAllConfigurations()
}

tasks.withType<org.cyclonedx.gradle.CyclonedxDirectTask>().configureEach {
    xmlOutput.unsetConvention()
}

tasks.named<JavaCompile>("compileTestJava") {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(
                tasks.named<org.cyclonedx.gradle.CyclonedxDirectTask>("cyclonedxDirectBom")
                    .flatMap { it.jsonOutput }
            ) {
                classifier = "cyclonedx"
                extension = "json"
            }
            pom {
                name.set(title)
                description.set(project.description)
                url.set(website)
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                organization {
                    name.set("Allette Systems")
                    url.set("https://www.allette.com.au")
                }
                scm {
                    url.set("git@github.com:pageseeder/$gitName.git")
                    connection.set("scm:git:git@github.com:pageseeder/$gitName.git")
                    developerConnection.set("scm:git:git@github.com:pageseeder/$gitName.git")
                }
                developers {
                    developer { name.set("Christophe Lauret"); email.set("clauret@weborganic.com") }
                    developer { name.set("Jean-Baptiste Reure"); email.set("jbreure@weborganic.com") }
                    developer { name.set("Carlos Cabral"); email.set("ccabral@allette.com.au") }
                }
            }
        }
    }
    repositories {
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}

sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "pageseeder")
        property("sonar.projectKey", "pageseeder_bastille")
        property("sonar.token", providers.gradleProperty("sonarcloud.login").getOrElse(""))
        property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/jacoco/test/jacocoTestReport.xml")
    }
}

jreleaser {
    configFile.set(file("jreleaser.toml"))
}

tasks.wrapper {
    gradleVersion = "9.6.1"
    distributionType = Wrapper.DistributionType.ALL
}
