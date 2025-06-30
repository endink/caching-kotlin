

dependencies{
    api(project(":core-starter"))
    api(project(":redis"))
    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-smile")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.esotericsoftware:kryo")
}