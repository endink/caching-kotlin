dependencies {
    api(project(":core"))
    api("io.lettuce:lettuce-core")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-smile")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    compileOnly("com.esotericsoftware:kryo")
}