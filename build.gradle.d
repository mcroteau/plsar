
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.6.20'
    id 'org.jetbrains.dokka' version '1.6.20'
    id 'maven-publish'
    id 'application'
    id 'signing'
    id 'jacoco'
}

repositories {
    mavenCentral()
}

group = "net.plsar"
version = "0.003"

application{
    mainClass.set("example.MainKt")
}

dependencies {
    implementation "com.h2database:h2:2.1.210"
    implementation "org.jetbrains.kotlin:kotlin-stdlib"
    implementation "org.jetbrains.kotlin:kotlin-reflect:1.6.10"
    implementation "org.jacoco:org.jacoco.core:0.8.7"
    testImplementation "org.junit.jupiter:junit-jupiter-api:5.8.2"
    testImplementation "org.junit.jupiter:junit-jupiter-engine:5.8.2"
}

//
//publishing {
//    repositories {
//        maven(MavenPublication) {
//            groupId = 'plsar.net'
//            artifactId = 'plsar'
//            version = '0.001'
//            components.kotlin
//
//            repositories {
//                maven {
//                    def releaseRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
//                    def snapshotRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
//                    url = project.hasProperty('release') ? releaseRepoUrl : snapshotRepoUrl
//                }
//            }
//
//            pom {
//                name "plsar"
//                packaging "jar"
//                description "PLSAR web framework"
//                url "plsar.net"
//
//                scm {
//                    connection "scm:git:git://github.com/mcroteau/plsar.git"
//                    developerConnection "scm:git:git://github.com/mcroteau/plsar.git"
//                    url "https://github.com/mcroteau/plsar"
//                }
//
//                licenses {
//                    license {
//                        name "MIT Licensne"
//                        url "https://opensource.org/licenses/MIT"
//                    }
//                }
//
//                developers {
//                    developer {
//                        id "mike"
//                        name "Mike Croteau"
//                        email "croteau.mike@gmail.com"
//                    }
//                }
//            }
//        }
//    }
//}
//
//signing {
//    sign publishing.publications.mavenJava
//}