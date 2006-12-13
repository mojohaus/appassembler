package org.codehaus.mojo.appassembler.daemon;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.JvmSettings;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface DaemonGeneratorService
{
    String ROLE = DaemonGeneratorService.class.getName();

    void generateDaemon( String platform, File stubDescriptor, File outputDirectory, MavenProject mavenProject,
                         ArtifactRepository localRepository )
        throws DaemonGeneratorException;

    void generateDaemon( String platform, File stubDescriptor, Daemon stubDaemon, File outputDirectory,
                         MavenProject mavenProject, ArtifactRepository localRepository )
        throws DaemonGeneratorException;

    Daemon mergeDaemons( Daemon dominant, Daemon recessive )
        throws DaemonGeneratorException;

    Daemon loadModel( File stubDescriptor )
        throws DaemonGeneratorException;

    /**
     * @param daemon The daemon to validate
     * @param descriptor An optional file reference that will be used in the exception messages.
     * @throws DaemonGeneratorException
     */
    void validateDaemon( Daemon daemon, File descriptor )
        throws DaemonGeneratorException;
}
