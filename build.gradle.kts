import com.github.gradle.node.npm.task.NpmTask

plugins {
    val kotlinVersion = "2.1.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.node-gradle.node") version "7.1.0"
    id("application")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_23)
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_23
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
    implementation("org.liquibase:liquibase-core")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("com.github.elimxim.flashcardsinspace.AppKt")
    applicationName = "flash"
}

sourceSets {
    main {
        resources {
            exclude("**/application-dev.yaml")
        }
    }
}

springBoot {
    mainClass.set("com.github.elimxim.flashcardsinspace.AppKt")
}

node {
    version = "22.17.0"
    npmVersion = "10.9.2"
    download = true
    nodeProjectDir = file("src/main/vue")
}

tasks.register<NpmTask>("npmRunBuild") {
    dependsOn("npmInstall")
    npmCommand.set(listOf("run", "build"))
    inputs.dir("src/main/vue")
    outputs.dir(project.layout.buildDirectory.dir("/resources/main/static/").get().asFile.toString())
}

tasks.named("processResources") {
    dependsOn("npmRunBuild")
}

tasks.withType<Test> {
    useJUnitPlatform()
}