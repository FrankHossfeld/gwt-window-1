import java.time.Year
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    id("net.ltgt.errorprone") version "0.8.1"
    id("com.github.sherter.google-java-format") version "0.8"
    id("org.jlleitschuh.gradle.ktlint") version "8.2.0"
    id("com.github.hierynomus.license") version "0.15.0"
    id("local.maven-publish")
}

group = "org.gwtproject.user.window"
version = "HEAD-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.3.3")
    errorproneJavac("com.google.errorprone:javac:9+181-r4173-1")

    api("org.gwtproject.event:gwt-logical-event:HEAD-SNAPSHOT")
    api("org.gwtproject.http:gwt-http:HEAD-SNAPSHOT")
    implementation("com.google.elemental2:elemental2-dom:1.0.0-RC1")
    implementation("com.google.elemental2:elemental2-core:1.0.0-RC1")
    implementation("com.google.jsinterop:base:1.0.0-RC1")

    testImplementation("junit:junit:4.12")
    testImplementation("com.google.gwt:gwt-user:2.8.2")
    testImplementation("com.google.gwt:gwt-dev:2.8.2")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(arrayOf("-Werror", "-Xlint:all"))
    if (JavaVersion.current().isJava9Compatible) {
        options.compilerArgs.addAll(arrayOf("--release", java.sourceCompatibility.majorVersion))
    }
    options.errorprone.check("StringSplitter", CheckSeverity.OFF)
}

tasks {
    jar {
        from(sourceSets["main"].allJava)
    }

    test {
        val warDir = file("$buildDir/gwt/www-test")
        val workDir = file("$buildDir/gwt/work")
        val cacheDir = file("$buildDir/gwt/cache")
        doFirst {
            mkdir(warDir)
            mkdir(workDir)
            mkdir(cacheDir)
        }

        classpath += sourceSets["main"].allJava.sourceDirectories + sourceSets["test"].allJava.sourceDirectories
        include("**/*Suite.class")
        systemProperty(
            "gwt.args",
            "-ea -draftCompile -batch module -war \"$warDir\" -workDir \"$workDir\" -runStyle HtmlUnit:Chrome"
        )
        systemProperty("gwt.persistentunitcachedir", cacheDir)
    }

    javadoc {
        (options as CoreJavadocOptions).apply {
            addBooleanOption("Xdoclint:all,-missing", true)
            // Workaround for https://github.com/gradle/gradle/issues/5630
            addStringOption("sourcepath", "")
        }
    }
}

googleJavaFormat {
    toolVersion = "1.7"
}
ktlint {
    version.set("0.34.2")
    enableExperimentalRules.set(true)
}

license {
    header = rootProject.file("LICENSE.header")
    encoding = "UTF-8"
    skipExistingHeaders = true
    mapping("java", "SLASHSTAR_STYLE")

    (this as ExtensionAware).extra["year"] = Year.now()
    (this as ExtensionAware).extra["name"] = "The GWT Project Authors"
}
