plugins {
    `java-library`
    `maven-publish`
    jacoco
    alias(libs.plugins.cyclonedx)
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.versions)
}

val title: String by project
val website: String by project
val gitName: String by project

group       = "org.pageseeder.bastille"
version     = file("version.txt").readText().trim()
description = title

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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

    compileOnly(libs.logback.core)
    compileOnly(libs.logback.classic)
    compileOnly(libs.servlet.api)

    runtimeOnly(libs.saxon)
}

tasks.withType<org.cyclonedx.gradle.CyclonedxDirectTask>().configureEach {
    xmlOutput.unsetConvention()
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
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
    gradleVersion = "8.14.1"
    distributionType = Wrapper.DistributionType.ALL
}
