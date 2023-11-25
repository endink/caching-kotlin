rootProject.name = "caching-kotlin"
include("core")
include("redis")
include("core-starter")
include("redis-starter")

pluginManagement {

    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
