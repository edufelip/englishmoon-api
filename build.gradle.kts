import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.spring") version "2.0.20"
    kotlin("plugin.jpa") version "2.0.20"
    id("org.openapi.generator") version "7.5.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

group = "com.englishmoon"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.flywaydb:flyway-core")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.22")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val openApiSpec = layout.projectDirectory.file("../openapi.yaml")

tasks.register<GenerateTask>("generateOpenApiKotlin") {
    generatorName.set("kotlin-spring")
    inputSpec.set(openApiSpec.asFile.absolutePath)
    outputDir.set("$buildDir/generated/openapi")
    apiPackage.set("com.englishmoon.generated.api")
    modelPackage.set("com.englishmoon.generated.model")
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useSpringBoot3" to "true",
            "enumPropertyNaming" to "UPPERCASE",
            "serviceInterface" to "true",
            "skipDefaultInterface" to "true",
        ),
    )
    generateApiTests.set(false)
    generateModelTests.set(false)
}

sourceSets {
    val main by getting {
        java.srcDir("$buildDir/generated/openapi/src/main/kotlin")
    }
}

tasks.named("compileKotlin") {
    dependsOn("generateOpenApiKotlin")
}

ktlint {
    filter {
        exclude("**/generated/**")
        exclude { it.file.path.contains("$buildDir/generated") }
    }
}

tasks.named("runKtlintCheckOverMainSourceSet") {
    dependsOn("generateOpenApiKotlin")
    mustRunAfter("generateOpenApiKotlin")
}

tasks.test {
    useJUnitPlatform {
        if (project.findProperty("skipIntegrationTests") == "true") {
            excludeTags("integration")
        }
    }
}

val integrationTest by tasks.registering(Test::class) {
    description = "Runs integration tests with Testcontainers (tagged 'integration')."
    group = "verification"

    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    shouldRunAfter(tasks.test)

    useJUnitPlatform {
        includeTags("integration")
    }

    onlyIf { project.findProperty("skipIntegrationTests") != "true" }
}

tasks.check {
    dependsOn(integrationTest)
}
