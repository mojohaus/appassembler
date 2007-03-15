package org.codehaus.mojo.appassembler.daemon.booter;

import java.io.File;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerator;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * Generates unix and/or windows wrapperscripts.
 * 
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 * @plexus.component role-hint="booter-unix"
 */
public class UnixBooterDaemonGenerator extends AbstractLogEnabled implements DaemonGenerator
{
    /**
     * @plexus.requirement role-hint="generic"
     */
    DaemonGenerator genericDaemonGenerator;

    public void generate( Daemon descriptor, MavenProject project, ArtifactRepository localRepository,
                          File outputDirectory ) throws DaemonGeneratorException
    {
        
        ScriptUtils.createBinScript( false, descriptor, project, localRepository, outputDirectory );
        genericDaemonGenerator.generate( descriptor, project, localRepository, new File( outputDirectory, "etc" ) );

    }

}
