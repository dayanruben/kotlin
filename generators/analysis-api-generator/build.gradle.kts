plugins {
    kotlin("jvm")
    id("jps-compatible")
}

sourceSets {
    "main" { java.srcDirs("main") }
    "test" { projectDefault() }
}

dependencies {
    compile(kotlinStdlib("jdk8"))

    testCompile(projectTests(":generators:test-generator"))
    testCompile(projectTests(":compiler:tests-common"))
    testCompile(projectTests(":compiler:tests-spec"))
    testCompile(projectTests("::analysis:low-level-api-fir"))
    testCompile(projectTests(":analysis:analysis-api-fir"))
    testCompile(intellijCoreDep()) { includeJars("intellij-core", "guava", rootProject = rootProject) }
    testApiJUnit5()
}

val generateFrontendApiTests by generator("org.jetbrains.kotlin.generators.tests.analysis.api.GenerateTestsKt")
