import java.io.*
import java.util.*
import org.codehaus.plexus.util.Os

os = new Os()

File cmd

File appBasedir = new File( basedir, "target/generated-resources/appassembler/booter-unix" )

if(Os.isFamily("unix")) {
  cmd = new File( basedir, "test-run.sh")
  ant.exec( command: "chmod +x " + cmd.absolutePath)
}
else {
  cmd = new File( basedir, "test-run.bat")
}

File propertiesFile = new File( basedir, "target/output.properties" );

command = cmd.absolutePath + " " + propertiesFile.absolutePath
println "Executing: " + command
ant.exec(command: command )
println "Executed"

Properties properties = new Properties();
FileInputStream input = new FileInputStream(propertiesFile)
properties.load(input)
input.close()

println new java.util.TreeMap(properties).toString().replace(',', '\n')

return properties.get("basedir").equals(appBasedir.absolutePath) &&
       properties.get("app.name").equals("hello-world")
