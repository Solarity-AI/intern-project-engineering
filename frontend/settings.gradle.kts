pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ProductReview"
include(":app")

// Include android-fw framework as composite build
includeBuild("../../android-fw") {
    dependencySubstitution {
        substitute(module("com.solarityai:fw-core")).using(project(":fw-core"))
        substitute(module("com.solarityai:fw-logging")).using(project(":fw-logging"))
        substitute(module("com.solarityai:fw-networking")).using(project(":fw-networking"))
        substitute(module("com.solarityai:fw-dto")).using(project(":fw-dto"))
        substitute(module("com.solarityai:fw-pagination")).using(project(":fw-pagination"))
        substitute(module("com.solarityai:fw-validation")).using(project(":fw-validation"))
        substitute(module("com.solarityai:fw-ui")).using(project(":fw-ui"))
    }
}
