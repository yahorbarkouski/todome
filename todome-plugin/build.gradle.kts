plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.2.0"
}

version = "1.0.1"
group = "com.yahorbarkouski.todome"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13")
}

gradlePlugin {
    website.set("https://github.com/yahorbarkouski/todome")
    vcsUrl.set("https://github.com/yahorbarkouski/todome")
    val todome by plugins.creating {
        id = "com.yahorbarkouski.todome"
        implementationClass = "com.yahorbarkouski.todome.ToDoMePlugin"
        displayName = "ToDoMe Plugin"
        description = """
            Gradle plugin for those who mean business. No more optional TODOs. No more forgotten tasks. 
            ToDoMe enforces due dates on every TODO and fails your build if you miss them. Code with conviction. 
            Meet your deadlines. Keep your promises.
        """.trimIndent()
        tags.set(listOf("todo", "java", "kotlin", "groovy", "linter", ))
    }
}

val functionalTest by sourceSets.creating
gradlePlugin.testSourceSets(functionalTest)

configurations[functionalTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())

val functionalTestTask = tasks.register<Test>("functionalTest") {
    testClassesDirs = functionalTest.output.classesDirs
    classpath = configurations[functionalTest.runtimeClasspathConfigurationName] + functionalTest.output
}

tasks.check {
    dependsOn(functionalTestTask)
}
