plugins {
	id("com.github.davidmc24.gradle.plugin.avro") version("1.0.0")
	id("org.springframework.boot") version("2.5.3")
	id("io.spring.dependency-management") version("1.0.11.RELEASE")
	//id("com.gorylenko.gradle-git-properties") version("2.3.1")
	java
	jacoco
    id("com.github.johnrengelman.processes") version("0.5.0")
    id("org.springdoc.openapi-gradle-plugin") version("1.3.3")
}

openApi {
    apiDocsUrl.set("http://localhost:8080/v3/api-docs")
    outputDir.set(file("$buildDir/api-doc"))
    outputFileName.set("openapi.json")
    waitTimeInSeconds.set(10)
    // forkProperties.set("-Dspring.profiles.active=special")
    // groupedApiMappings.set(["https://localhost:8080/v3/api-docs/groupA" to "swagger-groupA.json",
    //                         "https://localhost:8080/v3/api-docs/groupB" to "swagger-groupB.json"])
}

group = "com.happymoney"

java.sourceCompatibility = JavaVersion.VERSION_11

avro {
    fieldVisibility.set("PRIVATE")
}


//gitProperties {
//    keys = listOf("git.branch", "git.commit.id", "git.commit.id.abbrev", "git.commit.time", "git.tags", "git.closest.tag.name")
//}

repositories {
	mavenCentral()
	maven("https://packages.confluent.io/maven/")
}

sourceSets {
	create("intTest") {
		compileClasspath += sourceSets.main.get().output
		runtimeClasspath += sourceSets.main.get().output

		compileClasspath += sourceSets.test.get().output
		runtimeClasspath += sourceSets.test.get().output

	}
}

configurations {
    all {
        exclude(mapOf("group" to "org.springframework.boot", "module" to "spring-boot-starter-logging"))
    }
}

configurations["intTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["intTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())


//Fix for critical Log4j vulnerability https://nvd.nist.gov/vuln/detail/CVE-2021-45046
ext["log4j2.version"] = "2.17.0"

dependencies {
	// Platforms
	// implementation(platform("org.springframework.boot:spring-boot-dependencies:2.5.2.RELEASE"))

	// Spring dependencies
	implementation("org.springframework.boot:spring-boot-starter")

	// Salesforce SDK
	implementation("com.frejo:force-rest-api:0.0.43")


	//openapi
	implementation("org.springdoc:springdoc-openapi-ui:1.6.1")

	// Actuator dependencies
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// Logging dependencies
	implementation("org.springframework.boot:spring-boot-starter-log4j2")

	// Validation and devtools dependencies
	implementation("org.springframework.boot:spring-boot-starter-validation")
	//developmentOnly("org.springframework.boot:spring-boot-devtools")

	//database
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.postgresql:postgresql:42.2.24")
	//implementation("com.goterl:lazysodium-java:5.1.1")
	//implementation("net.java.dev.jna:jna:5.9.0")

	implementation("com.vladmihalcea:hibernate-types-52:2.14.0")

	// Data binding and format dependencies
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
	implementation("com.fasterxml.jackson.core:jackson-databind")

	//AWS dependencies
	//implementation(platform("com.amazonaws:aws-java-sdk-bom:1.11.1000"))
	//implementation("com.amazonaws:aws-java-sdk-s3")

	//Gson
	implementation("com.google.code.gson:gson:2.8.6")


	// Kafka dependencies
	implementation("org.springframework.kafka:spring-kafka")
	implementation("org.apache.avro:avro:1.10.0")
	implementation("io.confluent:kafka-avro-serializer:5.5.1")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-avro:2.12.5")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.5")

	// Testing dependencies
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(mapOf("group" to "org.junit.vintage", "module" to "junit-vintage-engine"))
	}
	//testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
	testImplementation("org.mockito:mockito-core:3.7.7")
	testImplementation("org.mockito:mockito-junit-jupiter:3.7.7")


	// Use JUnit Jupiter Engine for testing.
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	//implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	//implementation("org.springframework.boot:spring-boot-starter-security")
	//implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.kafka:spring-kafka")
	//implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5")

	implementation ("org.springframework.boot:spring-boot-starter-data-mongodb")

	implementation("org.projectlombok:lombok:1.18.20")
	annotationProcessor("org.projectlombok:lombok:1.18.20")
	testRuntimeOnly("org.projectlombok:lombok:1.18.20")
	testAnnotationProcessor("org.projectlombok:lombok:1.18.20")

	//mapstruct
	implementation("org.mapstruct:mapstruct:1.5.2.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.2.Final")
	testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.5.2.Final")
	//Retrofit
	implementation("com.squareup.retrofit2:retrofit:2.9.0")
	implementation("com.squareup.retrofit2:converter-gson:2.9.0")


	// OK HTTP
	implementation("com.squareup.okhttp3:okhttp:4.9.1")
	implementation("com.squareup.okhttp3:okhttp-tls:4.9.1")
	implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
	testImplementation ("com.squareup.okhttp3:mockwebserver:4.9.1")

	// Cache
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation ("com.github.ben-manes.caffeine:guava:3.0.3")

	//retry
	implementation("io.github.resilience4j:resilience4j-retry:1.7.1")

	implementation("io.github.hakky54:sslcontext-kickstart:6.7.0")
	implementation("io.github.hakky54:sslcontext-kickstart-for-pem:6.7.0")

	testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
	testImplementation("org.assertj:assertj-core:3.11.1")
	testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.27.0")

	//testcontainers
	testImplementation("org.testcontainers:testcontainers:1.16.0")
	//testImplementation("org.testcontainers:mongodb:1.16.0")
	testImplementation("org.testcontainers:junit-jupiter:1.16.0")
	testImplementation("org.testcontainers:kafka:1.16.0")
	testImplementation("org.testcontainers:postgresql:1.16.0")


	testImplementation("org.awaitility:awaitility:4.1.0")

}


tasks.compileJava {
	options.compilerArgs = listOf(
			"-Amapstruct.defaultComponentModel=spring",
			"-Amapstruct.unmappedTargetPolicy=IGNORE"
	)
}

tasks.bootRun {
	environment("spring_profiles_active", "local")
}

tasks.test {
    // Use junit platform for unit tests.
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    onlyIf { project.hasProperty("intTest")}

    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter("test")
}

tasks.check { dependsOn(integrationTest) }

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    finalizedBy(tasks.jacocoTestCoverageVerification)
    classDirectories.setFrom(
            sourceSets.main.get().output.asFileTree.matching {
                // File Patterns to exclude from testing & code coverage metrics
                exclude()
            }
    )
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            classDirectories.setFrom(sourceSets.main.get().output.asFileTree.matching {
                // File Patterns to exclude from testing & code coverage metrics
                exclude()
            })
            limit {
                // Minimum code coverage % for the build to pass
                minimum = "0.0".toBigDecimal()  //TODO: Raise this value
            }
        }
    }
}