plugins {
    kotlin("jvm") version "2.2.0"
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

group = "me.jordanfails.ascendduels"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://libraries.minecraft.net")
    maven("https://nexus.sirblobman.xyz/public")

    maven {
        url = uri("https://repo.selixe.com/repository/public-releases/")
        credentials {
            username = project.findProperty("nexusUsername") as String? ?: System.getenv("NEXUS_USERNAME")
            password = project.findProperty("nexusPassword") as String? ?: System.getenv("NEXUS_PASSWORD")
        }
    }

    maven {
        name = "lunarclient"
        url = uri("https://repo.lunarclient.dev")
    }

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

dependencies {
    implementation("services.plasma:honey:1.0.0")
    implementation("net.atlantismc.menu-api:bukkit:1.5")
    compileOnly("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    compileOnly("com.lunarclient:apollo-api:1.1.8")
    implementation("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
    implementation("com.github.KaboomB52:fanciful:0.4.0")
    compileOnly("com.github.sirblobman.api:core:2.7-SNAPSHOT")
    compileOnly("com.github.sirblobman.combatlogx:api:11.4-SNAPSHOT")
    implementation("io.papermc:paperlib:1.0.7")
    compileOnly(files("libs/core.jar"))
    compileOnly(files("libs/fawe.jar"))
    compileOnly(files("libs/EnchantmentAPI-4.28.jar"))
    compileOnly(files("libs/InfusedCommons-1.0-SNAPSHOT.jar"))
    compileOnly(files("libs/InfusedEnchants-1.0-SNAPSHOT.jar"))
    compileOnly(files("libs/worldguard-6.2.jar"))
    compileOnly(files("libs/WorldEdit.jar"))
    compileOnly(files("libs/SaberFactions.jar"))
    compileOnly(files("libs/ProtocolLib.jar"))

}

kotlin {
    jvmToolchain(8)
}