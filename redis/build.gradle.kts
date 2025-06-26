infra {
    useKotlinSerializationPlugin()
}
dependencies {
    api(project(":core"))
    api("io.lettuce:lettuce-core")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("com.fasterxml.jackson.core:jackson-databind")

    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-protobuf")
    compileOnly("com.esotericsoftware:kryo")
    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-smile")


    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf")
    testImplementation("com.esotericsoftware:kryo")

    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-smile")

    testImplementation("org.slf4j:slf4j-simple")
}
