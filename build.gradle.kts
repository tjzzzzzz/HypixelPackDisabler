plugins {
    id("net.fabricmc.fabric-loom") version "1.17.16"
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
}

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
val devAuthVersion: String = property("devauth_version") as String

version = "$modVersion+$minecraftVersion"
group = mavenGroup
base.archivesName.set(archivesBaseName)

repositories {
    exclusiveContent {
        forRepository {
            maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") {
                name = "DevAuth"
            }
        }
        filter { includeGroup("me.djtheredstoner") }
    }

    mavenCentral()
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
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:$loaderVersion")

    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    implementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

    bundled("io.ktor:ktor-client-core:$ktorVersion") { slim() }
    bundled("io.ktor:ktor-client-cio:$ktorVersion") { slim() }
    bundled("io.ktor:ktor-client-content-negotiation:$ktorVersion") { slim() }
    bundled("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion") { slim() }

    runtimeOnly("me.djtheredstoner:DevAuth-fabric:$devAuthVersion")
}

afterEvaluate {
    bundled.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
        dependencies.add("include", artifact.moduleVersion.id.toString())
    }
}

loom {
    runs {
        named("client") {
            vmArgs(
                "-Dmixin.debug.export=true",
                "-Ddevauth.enabled=true",
                "-Ddevauth.account=main"
            )
        }

        remove(getByName("server"))
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
