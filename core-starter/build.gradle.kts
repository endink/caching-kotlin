dependencies {
    api(project(":core"))
    implementation("io.netty:netty-common")
    api("org.springframework.boot:spring-boot-starter-aop")
    compileOnly("org.springframework:spring-tx")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    testImplementation("org.springframework:spring-tx")
    testImplementation("com.h2database:h2")
}

graalvmNative {
    testSupport.set(true)
}