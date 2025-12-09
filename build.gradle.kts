plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.7"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
}

group = "com.richbars"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	testImplementation("com.ninja-squad:springmockk:4.0.2")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("io.mockk:mockk:1.13.10")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")


	implementation("com.squareup.okhttp3:okhttp:4.12.0")
	implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
	implementation("org.json:json:20240303")

	implementation("org.postgresql:r2dbc-postgresql")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.9.0")
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

	implementation("me.paulschwarz:spring-dotenv:4.0.0")

	implementation(platform("software.amazon.awssdk:bom:2.25.31"))
	implementation("software.amazon.awssdk:sqs")
	implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs:3.2.0")


	implementation(platform("com.google.cloud:libraries-bom:26.30.0"))
	implementation("com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0") {
	implementation("com.google.auth:google-auth-library-oauth2-http")

	implementation("org.springframework.boot:spring-boot-starter-json")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

	implementation("com.google.api-client:google-api-client:2.7.2")
	implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

configurations.all {
	exclude(group = "commons-logging", module = "commons-logging")
	exclude(group = "com.vaadin.external.google", module = "android-json")
}}