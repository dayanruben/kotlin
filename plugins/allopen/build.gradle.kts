description = "Kotlin AllOpen Compiler Plugin"

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    embedded(project(":kotlin-allopen-compiler-plugin.common")) { isTransitive = false }
    embedded(project(":kotlin-allopen-compiler-plugin.cli")) { isTransitive = false }
    embedded(project(":kotlin-allopen-compiler-plugin.k1")) { isTransitive = false }
    embedded(project(":kotlin-allopen-compiler-plugin.k2")) { isTransitive = false }

    testImplementation(project(":kotlin-allopen-compiler-plugin"))
    testImplementation(project(":kotlin-allopen-compiler-plugin.common"))
    testImplementation(project(":kotlin-allopen-compiler-plugin.k1"))
    testImplementation(project(":kotlin-allopen-compiler-plugin.k2"))
    testImplementation(project(":kotlin-allopen-compiler-plugin.cli"))
    testImplementation(project(":compiler:backend"))
    testImplementation(project(":compiler:cli"))

    testImplementation(intellijCore())

    testApiJUnit5()
    testImplementation(projectTests(":compiler:tests-common-new"))
    testImplementation(projectTests(":compiler:test-infrastructure"))
    testImplementation(projectTests(":compiler:test-infrastructure-utils"))
    testImplementation(project(":compiler:fir:checkers"))
    testRuntimeOnly(project(":compiler:fir:fir-serialization"))

    testCompileOnly(project(":kotlin-reflect-api"))
    testRuntimeOnly(project(":kotlin-reflect"))
    testRuntimeOnly(project(":core:descriptors.runtime"))
}

sourceSets {
    "main" { none() }
    "test" {
        projectDefault()
        generatedTestDir()
    }
}

runtimeJar()
sourcesJar()
javadocJar()
testsJar()

projectTest(parallel = true) {
    workingDir = rootDir
    useJUnitPlatform()
}
