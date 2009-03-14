import java.io.*
import java.util.*
import org.codehaus.plexus.util.Os

os = new Os()

File cmd

File appBasedir = new File( basedir, "target/appassembler" )

if(Os.isFamily("unix")) {
  cmd = new File( basedir, "target/appassembler/bin/hello-world")
  ant.exec(command: "chmod +x " + cmd)
}
else {
  cmd = new File( basedir, "target/appassembler/bin/hello-world.bat")
}

File propertiesFile = new File( basedir, "target/output.properties" );

ant.exec(command: cmd.absolutePath + " " + propertiesFile.absolutePath )

Properties properties = new Properties();
FileInputStream input = new FileInputStream(propertiesFile)
properties.load(input)
input.close()

return properties.get("basedir").equals(appBasedir.absolutePath) &&
       properties.get("app.name").equals("hello-world")
