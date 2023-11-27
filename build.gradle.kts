
plugins {
    id("com.labijie.infra") version Versions.infraPlugin
}

allprojects {
    group = "com.labijie"
    version = "1.4.0"

    infra {
        useDefault {
            includeSource = true
            includeDocument = true
            infraBomVersion = Versions.infraBom
            kotlinVersion = Versions.kotlin
        }
        usePublishPlugin()
    }
}
subprojects {
    if(!project.name.startsWith("dummy")){
        infra {
            publishing {
                pom {
                    description = "labijie caching library"
                    githubUrl("endink", "caching-kotlin")
                    artifactId {
                        if (it.name == "core") "caching-kotlin" else "caching-kotlin-" + it.name
                    }
                    developer("AndersXiao", "sharping@outlook.com")
                }

                toGithubPackages("endink", "caching-kotlin")
            }
        }
    }
}




