
plugins {
    id("com.labijie.infra") version Versions.infraPlugin
    id("org.graalvm.buildtools.native") version Versions.nativeBuildTool apply false
}

allprojects {
    group = "com.labijie"
    version = "1.5.0"

    infra {
        useDefault {
            includeSource = true
            includeDocument = true
            infraBomVersion = Versions.infraBom
        }
    }

}
subprojects {
    //apply(plugin = "org.graalvm.buildtools.native")


    if(!project.name.startsWith("dummy")){
        infra {
            plugins.apply("org.graalvm.buildtools.native")
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




