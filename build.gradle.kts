
plugins {
    id("com.labijie.infra") version Versions.infraPlugin
}

allprojects {
    group = "com.labijie"
    version = "1.3.0"

    infra {
        useDefault {
            includeSource = true
            infraBomVersion = Versions.infraBom
            kotlinVersion = Versions.kotlin
            useMavenProxy = false
        }

        useNexusPublish()
    }
}
subprojects {
    if(!project.name.startsWith("dummy")){
        infra {
            usePublish {
                description = "labijie caching library"
                githubUrl("endink", "caching-kotlin")
                artifactId {
                    if(it.name == "core") "caching-kotlin" else "caching-kotlin-" + it.name
                }
                developer("AndersXiao", "sharping@outlook.com")
            }
        }
    }
}




