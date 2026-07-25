import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask

buildscript {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        mavenCentral()
    }
    dependencies {
        classpath("net.fabricmc:fabric-loom:1.13.6")
    }
}

plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
}

apply(plugin = "fabric-loom")

val loom = the<LoomGradleExtensionAPI>()

val modVersion: String = property("mod_version") as String
val mavenGroup: String = property("maven_group") as String
val archivesBaseName: String = property("archives_base_name") as String

val minecraftVersion: String = property("minecraft_version") as String
val minecraftDependency: String = property("minecraft_dependency") as String
val javaVersion: Int = (property("java_version") as String).toInt()
val loaderVersion: String = property("loader_version") as String
val fabricApiVersion: String = property("fabric_api_version") as String
val fabricKotlinVersion: String = property("fabric_kotlin_version") as String
val ktorVersion: String = property("ktor_version") as String

version = "$modVersion+$minecraftVersion"
group = mavenGroup
base.archivesName.set(archivesBaseName)

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") { name = "Fabric" }
}

val bundled: Configuration = configurations.create("bundled") {
    isTransitive = true
}

configurations.implementation.get().extendsFrom(bundled)

fun ExternalModuleDependency.slim() {
    exclude(group = "org.jetbrains.kotlin")
    exclude(group = "org.slf4j")
}

dependencies {
    "minecraft"("com.mojang:minecraft:$minecraftVersion")
    "mappings"(loom.officialMojangMappings())
    "modImplementation"("net.fabricmc:fabric-loader:$loaderVersion")

    "modImplementation"("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    "modImplementation"("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

    bundled("io.ktor:ktor-client-core:$ktorVersion") { slim() }
    bundled("io.ktor:ktor-client-cio:$ktorVersion") { slim() }
    bundled("io.ktor:ktor-client-content-negotiation:$ktorVersion") { slim() }
    bundled("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion") { slim() }
}

afterEvaluate {
    bundled.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
        dependencies.add("include", artifact.moduleVersion.id.toString())
    }
}

tasks.processResources {
    val props = mapOf(
        "version" to version,
        "minecraft_version" to minecraftVersion,
        "minecraft_dependency" to minecraftDependency,
        "java_version" to javaVersion,
        "loader_version" to loaderVersion,
        "fabric_kotlin_version" to fabricKotlinVersion
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
}

kotlin {
    jvmToolchain(javaVersion)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}

tasks.withType<RemapJarTask>().configureEach {
    // ensure JIJ nested jars are included
}
