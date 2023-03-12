import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer


plugins {
    id("java-library")
    id("maven-publish")
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"

}

group = "de.pianoman911"
version = "1.0.0"

val main = "$group.${name.toLowerCase()}.main.${name}Main"
println("Main: $main")


repositories {
    mavenCentral()
}

dependencies {
    // Utilities
    api("com.google.guava:guava:31.1-jre")

    // gson
    api("com.google.code.gson:gson:2.8.9")

    // Logging
    val log4jVersion = "2.19.0"
    api("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    api("org.apache.logging.log4j:log4j-iostreams:$log4jVersion")
    api("org.apache.logging.log4j:log4j-core:$log4jVersion")
    api("org.apache.logging.log4j:log4j-jul:$log4jVersion")
    api("org.apache.logging.log4j:log4j-api:$log4jVersion")

    // Console handling
    api("net.minecrell:terminalconsoleappender:1.3.0")
    runtimeOnly("org.jline:jline-terminal-jansi:3.21.0")
    runtimeOnly("com.lmax:disruptor:3.4.4")

    // Argument parsing
    api("net.sf.jopt-simple:jopt-simple:5.0.4")

    // HOCON configuration parsing
    val configurateVersion = "4.1.2"
    api("org.spongepowered:configurate-core:$configurateVersion")
    api("org.spongepowered:configurate-yaml:$configurateVersion")
    api("org.spongepowered:configurate-xml:$configurateVersion")
    api("org.spongepowered:configurate-hocon:$configurateVersion")
    api("org.spongepowered:configurate-gson:$configurateVersion")

    // Compression for the archive actions
    api("org.apache.commons:commons-compress:1.21")

    // used for string replacing
    api("org.apache.commons:commons-text:1.10.0")

    // SQL
    api("org.mariadb.jdbc:mariadb-java-client:2.7.5")
    api("com.zaxxer:HikariCP:5.0.1")

    // Networking
    api("io.netty:netty-all:4.1.86.Final")

    // Annotations
    api("org.jetbrains:annotations:22.0.0")
}

tasks {
    jar {
        manifest.attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "pianoman911",
            "Multi-Release" to "true",
            "Main-Class" to main
        )
    }

    shadowJar {
        transform(Log4j2PluginsCacheFileTransformer::class.java)
        minimize()
    }

    build {
        dependsOn(shadowJar)
    }
}

application {
    mainClass.set(main)
}

java {
    withSourcesJar()
}

