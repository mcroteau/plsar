import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.20"
    id("org.jetbrains.dokka") version "1.6.10"
    id("maven-publish")
    id("java-library")
    id("signing")
    id("jacoco")
    id("application")
}

repositories {
    mavenCentral()
}

group = "net.plsar"
version = "0.003"

val dokkaOutputDir = "$buildDir/dokka"

application{
    mainClass.set("example.MainKt")
}

tasks.dokkaHtml {
    outputDirectory.set(file(dokkaOutputDir))
}

val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
    delete(dokkaOutputDir)
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
}

var sourcesJar : Jar? = null
tasks {
    val sources by creating(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        classifier = "sources"
        from(sourceSets["main"].allSource)
    }
    sourcesJar = sources
    artifacts {
        add("archives", sourcesJar)
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
}

tasks.test {
    extensions.configure(JacocoTaskExtension::class) {
        destinationFile = file("$buildDir/jacoco/jacoco.exec")
    }
    finalizedBy("jacocoTestReport")
}

jacoco {
    toolVersion = "0.8.7"
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = false
        csv.isEnabled = false
        html.isEnabled = true
        html.destination = file("$buildDir/reports/coverage")
    }
}

val coverage by tasks.registering {
    group = ""
    description = "Runs the unit tests with coverage"
    dependsOn(":test", ":jacocoTestReport")
    tasks["jacocoTestReport"].mustRunAfter(tasks["test"])
    tasks["jacocoTestCoverageVerification"].mustRunAfter(tasks["jacocoTestReport"])
}

dependencies {
    implementation("com.h2database:h2:2.1.210")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.jacoco:org.jacoco.core:0.8.7")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

publishing {
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = ""
                password = ""
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = "net.plsar"
            artifactId = "plsar"
            version = "0.003"
            from(components["kotlin"])
        }
        withType<MavenPublication> {
            artifact(javadocJar)
            artifact(sourcesJar)
            pom {
                name.set("plsar")
                description.set("A Kotlin web Framework")
                url.set("http://www.plsar.net")
                licenses {
                    license {
                        name.set("MIT license")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/mcroteau/plsar/issues")
                }
                scm {
                    connection.set("https://github.com/mcroteau/plsar.git")
                    url.set("https://github.com/mcroteau/plsar")
                }
                developers {
                    developer {
                        name.set("Mike Croteau")
                        email.set("croteau.mike@gmail.com")
                    }
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        "",
        ""
    )
    sign(publishing.publications)
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}