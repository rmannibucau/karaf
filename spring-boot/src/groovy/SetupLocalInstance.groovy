//
// just a helper script to have a ready to test instance:
// $  groovy src/groovy/SetupLocalInstance.groovy
//
// note: this set up the extension in startup bundles but shell-core bundle does not catch up already installed ones
//       so you can need to "restart <spring-boot-extension-pid>"
//
// If you are lazy:
// $  mvn clean install && groovy src/groovy/SetupLocalInstance.groovy && cd target/work-distribution && KARAF_DEBUG=true ./bin/karaf console
//
// then:
//
// karaf$ restart 15
// karaf$ spring-boot:install file:///${karaf.sources}/spring-boot/src/test/resources/rest-service-0.0.1-SNAPSHOT.jar
// karaf$ spring-boot:start rest-service-0.0.1-SNAPSHOT.jar
// karaf$ spring-boot:stop rest-service-0.0.1-SNAPSHOT.jar
//
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

final String karafVersion = '4.3.0.RC1'
final String asmVersion = '9.0-beta'

final Path here = Paths.get('.').normalize().toAbsolutePath()
if (!'spring-boot'.equals(here.getFileName().toString())) {
    throw new IllegalArgumentException('Wrong execution context')
}
final Path workdir = here.resolve("target/work-distribution").normalize().toAbsolutePath()
final Path m2 = Paths.get(System.getProperty('user.home')).resolve('.m2/repository')
final Path karafTarGz = m2.resolve("org/apache/karaf/apache-karaf/${karafVersion}/apache-karaf-${karafVersion}.tar.gz").normalize().toAbsolutePath()
final Path pom = Paths.get('pom.xml');
final GPathResult pomSluper = new XmlSlurper().parseText(pom.toFile().text.toString())
final String groupId = pomSluper.groupId.text()
final String artifactId = pomSluper.artifactId.text()
final String version = pomSluper.parent.version.text()
final String coords = "${groupId}/${artifactId}/${version}"
final Path springBootExtension = Files.list(Paths.get('target'))
        .filter(it -> it.getFileName().toString().startsWith('org.apache.karaf.') && it.getFileName().toString().endsWith(".jar"))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException('Ensure to build the extension first'))
final Path extensionTarget = workdir.resolve("system/${groupId.replace('.', '/')}/${artifactId}/${version}/${artifactId}-${version}.jar")
final File startupBundles = workdir.resolve('etc/startup.properties').toFile()

def addAsm(Path m2, Path workdir, String asmVersion, String bundle) {
    final Path asm = m2.resolve("org/ow2/asm/asm${bundle}/${asmVersion}/asm${bundle}-${asmVersion}.jar")
    final Path asmTarget = workdir.resolve("system/org/ow2/asm/asm${bundle}/${asmVersion}/asm${bundle}-${asmVersion}.jar")
    Files.createDirectories(asmTarget.parent)
    Files.copy(asm, asmTarget)
}

final AntBuilder ant = new AntBuilder()
ant.delete(dir: workdir.toString())
ant.untar(src: karafTarGz.toString(), dest: workdir, overwrite: 'true', compression: 'gzip') {
    mapper {
        globmapper(from: "apache-karaf-${karafVersion}/*", to: '*')
    }
}

startupBundles.text = startupBundles.text.trim() + "\n" +
        "mvn\\:org.ow2.asm/asm/${asmVersion} = 16\n" +
        "mvn\\:org.ow2.asm/asm-tree/${asmVersion} = 16\n" +
        "mvn\\:org.ow2.asm/asm-commons/${asmVersion} = 16\n" +
        "mvn\\:${coords} = 16\n"

['karaf', 'inc'].forEach { workdir.resolve("bin/${it}").toFile().setExecutable(true) }
addAsm(m2, workdir, asmVersion, '')
addAsm(m2, workdir, asmVersion, '-tree')
addAsm(m2, workdir, asmVersion, '-commons')
Files.createDirectories(extensionTarget.parent)
Files.copy(springBootExtension, extensionTarget)

println("Distribution ready in ${workdir}")