/**
 *
 * The MIT License
 *
 * Copyright 2006-2016 The MojoHaus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.codehaus.mojo.appassembler.daemon.yajsw;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerator;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.util.DependencyFactory;
import org.codehaus.mojo.appassembler.util.FormattedProperties;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author Sandra Parsick
 * @version $Id$
 * @plexus.component role-hint="yajsw"
 */
public class YetAnotherJavaServiceWrapperDaemonGenerator
    extends AbstractLogEnabled
    implements DaemonGenerator
{

    private static List<String> CORE_LIBS = new ArrayList<String>()
    {
        {
            add("commons/commons-cli-1.4.jar");
            add("commons/commons-collections-3.2.2.jar");
            add("commons/commons-configuration2-2.3.jar");
            add("commons/commons-io-2.6.jar");
            add("commons/commons-lang3-3.8.1.jar");
            add("commons/commons-lang-2.6.jar");
            add("commons/commons-logging-1.1.jar");
            add("commons/commons-text-1.6.jar");
            add("commons/commons-vfs2-2.2.jar");
            add("jna/jna-5.3.1.jar");
            add("jna/jna-platform-5.3.1.jar");
            add("netty/netty-all-4.1.36.Final.jar");
            add("yajsw/ahessian.jar");
        }
    };

    private static List<String> EXTENDED_LIBS = new ArrayList<String>()
    {
        {
            add("velocity/velocity-1.7.jar");
            add("yajsw/hessian4.jar");
            add("groovy/groovy-2.5.7.jar");
            add("groovy/groovy-patch.jar");
        }
    };

    private static List<String> BAT_SCRIPTS = new ArrayList<String>()
    {
        {
              add("installService.bat");
              add("runConsole.bat");
              add("startService.bat");
              add("stopService.bat");
              add("uninstallService.bat");
              add("setenv.bat");
              add("wrapper.bat");
        }
    };

    private static List<String> SHELL_SCRIPTS = new ArrayList<String>()
    {
        {
            add("installDaemon.sh");
            add("runConsole.sh");
            add("startDaemon.sh");
            add("stopDaemon.sh");
            add("uninstallDaemon.sh");
            add("setenv.sh");
            add("wrapper.sh");
            add("installDaemonNoPriv.sh");
            add("uninstallDaemonNoPriv.sh");
            add("startDaemonNoPriv.sh");
            add("stopDaemonNoPriv.sh");
        }
    };

    private static List<String> GROOVY_SCRIPTS = new ArrayList<String>()
    {
        {
            add("trayMessage.gv");
        }
    };

    private static List<String> TEMPLATES = new ArrayList<String>()
    {
        {
            add("daemon.vm");
        }
    };

    @Override
    public void generate( DaemonGenerationRequest request ) throws DaemonGeneratorException
    {
        Daemon daemon = request.getDaemon();

        File outputDirectory = new File( request.getOutputDirectory(), daemon.getId() );

        writeWrapperConfFile(outputDirectory, daemon, request);

        writeScripts(outputDirectory);
        writeLibraries(outputDirectory);


    }

    private void writeWrapperConfFile(File outputDirectory, Daemon daemon, DaemonGenerationRequest request) throws DaemonGeneratorException
    {
        InputStream in = this.getClass().getResourceAsStream( "conf/wrapper.conf.default" );

        if ( in == null )
        {
            throw new DaemonGeneratorException( "Could not load template." );
        }

        FormattedProperties confFile = new FormattedProperties();

        try
        {
            confFile.read( in );
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Error reading template: " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( in );
        }

        confFile.setProperty("wrapper.java.app.mainclass", daemon.getMainClass());
        confFile.setProperty("wrapper.working.dir", "${wrapper_home}");

        if( daemon.getJvmSettings() != null)
        {
            confFile.setProperty("wrapper.java.initmemory", daemon.getJvmSettings().getInitialMemorySize());
            confFile.setProperty("wrapper.java.maxmemory", daemon.getJvmSettings().getMaxMemorySize());
            createAdditional(daemon, confFile);
        }
        createClasspath(daemon, request, confFile);


        ByteArrayOutputStream string = new ByteArrayOutputStream();
        confFile.save( string );

        writeFile(new File(outputDirectory, "conf/wrapper.conf"), new ByteArrayInputStream( string.toByteArray() ));

    }

    private void createClasspath(Daemon daemon, DaemonGenerationRequest request, FormattedProperties confFile)
    {
        final String wrapperClassPathPrefix = "wrapper.java.classpath.";
        final String wrapperHome = "${wrapper_home}/";
        final String repositoryName = daemon.getRepositoryName() + "/";

        int counter = 1;
        confFile.setProperty( wrapperClassPathPrefix + counter++, wrapperHome + "wrapper.jar" );

        MavenProject project = request.getMavenProject();
        ArtifactRepositoryLayout layout = request.getRepositoryLayout();

        confFile.setProperty( wrapperClassPathPrefix + counter++, wrapperHome + repositoryName
                + DependencyFactory.create( project.getArtifact(), layout, true,
                request.getOutputFileNameMapping() ).getRelativePath() );

        Iterator j = project.getRuntimeArtifacts().iterator();
        while ( j.hasNext() )
        {
            Artifact artifact = (Artifact) j.next();

            confFile.setProperty( wrapperClassPathPrefix + counter, wrapperHome + repositoryName
                    + DependencyFactory.create( artifact, layout, daemon.isUseTimestampInSnapshotFileName(),
                    request.getOutputFileNameMapping() ).getRelativePath() );
            counter++;
        }

    }

    private void createAdditional( Daemon daemon, FormattedProperties confFile )
    {
        int count = 1;
        for (Iterator i = daemon.getJvmSettings().getSystemProperties().iterator(); i.hasNext(); count++ )
        {
            String systemProperty = (String) i.next();
            confFile.setProperty( "wrapper.java.additional." + count, "-D" + systemProperty );
        }
        for ( Iterator i = daemon.getJvmSettings().getExtraArguments().iterator(); i.hasNext(); count++ )
        {
            String extraArgument = (String) i.next();
            confFile.setProperty( "wrapper.java.additional." + count, extraArgument );
        }
    }


    private void writeScripts(File outputDirectory) throws DaemonGeneratorException
    {
        for (String batScriptName : BAT_SCRIPTS)
        {
            String batScriptPath = "bat/" + batScriptName;
            InputStream batScript = createInputStream(batScriptPath);
            writeFile(new File(outputDirectory, batScriptPath), batScript);
        }

        for(String shellScriptName : SHELL_SCRIPTS)
        {
            String shellScriptPath = "bin/" + shellScriptName;
            InputStream shellScript = createInputStream(shellScriptPath);
            writeFile(new File(outputDirectory, shellScriptPath), shellScript);
        }

        for(String groovyScriptName : GROOVY_SCRIPTS)
        {
            String groovyScriptPath = "scripts/" + groovyScriptName;
            InputStream groovyScript = createInputStream(groovyScriptPath);
            writeFile(new File(outputDirectory, groovyScriptPath), groovyScript);
        }

        for(String templateScriptName : TEMPLATES) {
            String templateScriptPath = "templates/" + templateScriptName;
            InputStream templateScript = createInputStream(templateScriptPath);
            writeFile(new File(outputDirectory, templateScriptPath), templateScript);
        }
    }

    private void writeLibraries(File outputDirectory) throws DaemonGeneratorException
    {
        String wrapperJarName = "wrapper.jar";
        InputStream wrapperJar = createInputStream(wrapperJarName);
        writeFile(new File(outputDirectory, wrapperJarName), wrapperJar);

        String wrapperAppJarName = "wrapperApp.jar";
        InputStream wrapperAppJar = createInputStream(wrapperAppJarName);
        writeFile(new File(outputDirectory, wrapperAppJarName), wrapperAppJar);


        for (String core: CORE_LIBS)
        {
            String coreLibName = "lib/core/" + core;
            InputStream coreLib = createInputStream(coreLibName);
            writeFile(new File(outputDirectory, coreLibName), coreLib);
        }

        for(String extended : EXTENDED_LIBS)
        {
            String extendedLibName = "lib/extended/" + extended;
            InputStream extendedLib = createInputStream(extendedLibName);
            writeFile(new File(outputDirectory, extendedLibName), extendedLib);
        }
    }

    private InputStream createInputStream(String resourceName) throws DaemonGeneratorException
    {
        InputStream resource = this.getClass().getResourceAsStream(resourceName);
        if (resource == null)
        {
            throw new DaemonGeneratorException("Cannot load " + resourceName);
        }
        return resource;
    }

    private static void writeFile( File outputFile, InputStream inputStream )
            throws DaemonGeneratorException
    {
        try
        {
            FileUtils.copyInputStreamToFile(inputStream, outputFile);
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Error writing output file: " + outputFile.getAbsolutePath(), e );
        }
    }
    
}
