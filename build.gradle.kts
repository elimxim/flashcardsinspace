import com.github.gradle.node.npm.task.NpmTask

plugins {
    val kotlinVersion = "2.4.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.node-gradle.node") version "7.1.0"
    id("application")
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xemit-jvm-type-annotations")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
    implementation("com.brevo:brevo:1.1.11")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20261107.1")
    implementation("com.github.ben-manes.caffeine:caffeine:3.3.1")
    implementation("com.github.usefulness:webp-imageio:0.11.0")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.12")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.4.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("com.github.elimxim.flashcardsinspace.AppKt")
    applicationName = "flash"
    applicationDefaultJvmArgs = listOf(
        "-Xmx800m",
        "-Xms800m",
        "-XX:+UseSerialGC",
        "-XX:+UseCompactObjectHeaders",
        "--enable-native-access=ALL-UNNAMED",
        "-XX:+ExitOnOutOfMemoryError",
        "-XX:+HeapDumpOnOutOfMemoryError",
        "-XX:HeapDumpPath=/tmp/flashcardsinspace.hprof",
    )
}

springBoot {
    mainClass.set("com.github.elimxim.flashcardsinspace.AppKt")
}

node {
    version = "26.8.1"
    npmVersion = "11.19.0"
    download = true
    nodeProjectDir = file("src/main/vue")
}

tasks.register<NpmTask>("npmRunBuild") {
    dependsOn("npmInstall")
    npmCommand.set(listOf("run", "build"))
    inputs.dir("src/main/vue")
    outputs.dir(project.layout.buildDirectory.dir("resources/main/static/").get().asFile.toString())
}

tasks.register<NpmTask>("npmRunTest") {
    dependsOn("npmInstall")
    npmCommand.set(listOf("run", "test", "--", "--run"))
    inputs.dir("src/main/vue/src")
}

tasks.register<NpmTask>("npmRunLint") {
    dependsOn("npmInstall")
    npmCommand.set(listOf("run", "lint"))
    inputs.dir("src/main/vue/src")
}

tasks.register<NpmTask>("npmRunTypeCheck") {
    dependsOn("npmInstall")
    npmCommand.set(listOf("run", "type-check"))
    inputs.dir("src/main/vue/src")
}

tasks.named("processResources") {
    dependsOn("npmRunBuild")
}

tasks.withType<Test> {
    dependsOn("npmRunTest")
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
