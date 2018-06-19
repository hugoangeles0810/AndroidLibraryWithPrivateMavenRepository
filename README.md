# Android library with my own private Maven repository

En este post les mostraré la implementación que realicé para publicar mi Android Library a un repositorio de Maven privado.
El servidor de repositorios privado que elegí fue [Artifactory](https://jfrog.com/open-source/).

### Instalar y configurar Artifactory

La instalación de artifactory es trivial solo tenemos que descargarlo desde [aquí](https://jfrog.com/open-source/), descomprimimos el zip. Para iniciar Artifactory ingresamos a la carpeta bin

![Artifactory folder](https://github.com/hugoangeles0810/AndroidLibraryWithPrivateMaven/raw/art/image-01.png)
y buscamos el ejecutamos el archivo artifactory.sh o artifactory.bat dependiendo de nuestro sistema operativo
 
 ![Ejecutando artifactory](https://github.com/hugoangeles0810/AndroidLibraryWithPrivateMaven/raw/art/image-02.png)
 
 luego de iniciarse Artifactory nos vamos al siguiente [enlace](http://localhost:8081/) y veremos algo parecido a esto:
 
 ![Vista web de artifactory](https://github.com/hugoangeles0810/AndroidLibraryWithPrivateMaven/raw/art/image-03.png)
 
 la primera vez que se inicia Artifactory te solicitará que crees las credenciales de administrador, despues de ello podemos crear un repositorio, nos dirigimos a Admin > Repositories > Local y seleccionamos la opción de nuevo:
 
  ![Nuevo repositorio](https://github.com/hugoangeles0810/AndroidLibraryWithPrivateMaven/raw/art/image-04.png)
 
 seleccionamos Maven e ingresamos el Repo key, una vez creado el repositorio podremos verlo desde la pestaña Artifacts
 

 
 ### Configurar mi librería con Artifactory
 
 El primer paso es instalar gradle plugin de Artifactory en el build.gradle de nuestro proyecto de la libreria
```gradle
buildscript {
    ext.kotlin_version = '1.2.30'
    repositories {
        // TODO: 02 - Asegurate de tener jcenter en tus repositories
        jcenter()
        google()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.1.3'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        // TODO: 01 - Agregamos el plugin de artifactory
        classpath "org.jfrog.buildinfo:build-info-extractor-gradle:4.7.3"
    }
}
```

en el archivo build.gradle de nuestra librería importamos los plugins de Artifactory y Maven Publish
```gradle
apply plugin: 'com.android.library'

// TODO: 03 - Agregamos el plugin de Artifactory y Maven publish
apply plugin: 'com.jfrog.artifactory'
apply plugin: 'maven-publish'
```

luego en el mismo archivo build.gradle
```gradle
// TODO: 04 - Configuramos nuestra libreria para subir al repositorio maven
publishing {
    publications {
        aar(MavenPublication) {
            groupId group
            version version
            artifactId project.getName()

            artifact("$buildDir/outputs/aar/${project.getName()}-release.aar")


            // con este bloque de código agregamos las dependencias de nuestra libreria al pom.xml
            pom.withXml {
                def dependenciesNode = asNode().appendNode('dependencies')

                ext.addDependency = { Dependency dep, String scope ->
                    if (dep.group == null || dep.version == null || dep.name == null || dep.name == "unspecified")
                        return // ignore invalid dependencies

                    final dependencyNode = dependenciesNode.appendNode('dependency')
                    dependencyNode.appendNode('groupId', dep.group)
                    dependencyNode.appendNode('artifactId', dep.name)
                    dependencyNode.appendNode('version', dep.version)
                    dependencyNode.appendNode('scope', scope)

                    if (!dep.transitive) {
                        final exclusionNode = dependencyNode.appendNode('exclusions').appendNode('exclusion')
                        exclusionNode.appendNode('groupId', '*')
                        exclusionNode.appendNode('artifactId', '*')
                    } else if (!dep.properties.excludeRules.empty) {
                        final exclusionNode = dependencyNode.appendNode('exclusions').appendNode('exclusion')
                        dep.properties.excludeRules.each { ExcludeRule rule ->
                            exclusionNode.appendNode('groupId', rule.group ?: '*')
                            exclusionNode.appendNode('artifactId', rule.module ?: '*')
                        }
                    }
                }

                configurations.compile.getAllDependencies().each { dep -> addDependency(dep, "compile") }
                configurations.api.getAllDependencies().each { dep -> addDependency(dep, "compile") }
                configurations.implementation.getAllDependencies().each { dep -> addDependency(dep, "compile") }
            }
        }
    }
}
```

en el mismo archivo build.gradle de nuestra librería configuramos artifactory:
```gradle
// TODO: 05 - Configuramos artifactory
artifactory {
    contextUrl = "${artifactory_contextUrl}"   //The base Artifactory URL if not overridden by the publisher/resolver
    publish {
        repository {
            repoKey = 'libs-release-local' // Repo key que creamos en artifactory
            username = "${artifactory_user}"
            password = "${artifactory_password}"
            maven = true

        }

        defaults {
            publications('aar')
            publishArtifacts = true
            publishPom = true
        }
    }
    resolve {
        repository {
            repoKey = 'gradle-release'
            username = "${artifactory_user}"
            password = "${artifactory_password}"
            maven = true

        }
    }
}
```

para terminar con la configuración debemos agregar a nuestro gradle.properties nuestras crendenciales de artifactory
```properties
artifactory_user=admin
artifactory_password=AP2XWnFQ5d41y5XoYh42RKqgepi
artifactory_contextUrl=http://localhost:8081/artifactory
```
en el campo artifactory_password debemos colocar nuestra contraseña encriptada que la podemos obtener desde Artifactory en nuestro User Profile.

Una vez configurada la librería, para publicarla en nuestro servidor Maven debemos ejecutar la tarea artifactoryPublish
```bash
./gradlew build artifactoryPublish
```

### Como usar la librería

Para usar la librería solo agregamos nuestro repositorio de maven a nuestro build.gradle del app
```gradle
repositories {
    maven { url "http://localhost:8081/artifactory/libs-release-local" }
}
```

y solo falta agregar la dependencia de la librería
```gradle
repositories {
    maven { url "http://localhost:8081/artifactory/libs-release-local" }
}
```

### Conclusiones
Aprendimos a configurar nuestro propio Maven repostory y publicar nuestras librerias, esto es importante por que en ocaciones tenemos que desarrollar librerias privadas para nuestro equipo que se usan en diferentes proyectos por lo que es muy útil distribuir nuestra librería por medio de Maven. 