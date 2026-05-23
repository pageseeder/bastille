import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.plugins.signing.SigningExtension

val title: String by project
val website: String by project
val gitName: String by project

tasks.named<Javadoc>("javadoc") {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("mavenJava") {
            from(project.components["java"])

            pom {
                name.set(title)
                description.set(project.description)
                url.set(website)

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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
                    developer {
                        id.set("clauret")
                        name.set("Christophe Lauret")
                        email.set("clauret@weborganic.com")
                    }
                    developer {
                        id.set("jbreure")
                        name.set("Jean-Baptiste")
                        email.set("jbreure@weborganic.com")
                    }
                    developer {
                        id.set("ccabral")
                        name.set("Carlos Cabral")
                        email.set("ccabral@allette.com.au")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            val releaseRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (project.version.toString().endsWith("SNAPSHOT")) snapshotRepoUrl else releaseRepoUrl)
            name = "sonatype"
            credentials {
                username = project.findProperty("sonatypeUsername") as String?
                password = project.findProperty("sonatypePassword") as String?
            }
        }
    }
}

val publications = extensions.getByType<PublishingExtension>().publications
configure<SigningExtension> {
    sign(publications["mavenJava"])
}
