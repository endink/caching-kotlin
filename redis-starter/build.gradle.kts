dependencies{
    api(project(":core-starter"))
    api(project(":redis"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.esotericsoftware:kryo")
}