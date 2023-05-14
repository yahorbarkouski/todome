plugins {
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13")
}

gradlePlugin {
    val todome by plugins.creating {
        id = "com.yahorbarkouski.todome"
        implementationClass = "com.yahorbarkouski.todome.ToDoMePlugin"
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
