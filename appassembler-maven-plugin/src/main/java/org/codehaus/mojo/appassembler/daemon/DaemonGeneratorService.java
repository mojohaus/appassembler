package org.codehaus.mojo.appassembler.daemon;

import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.mojo.appassembler.model.generic.Daemon;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface DaemonGeneratorService
{
    String ROLE = DaemonGeneratorService.class.getName();

    void generateDaemon( String platform, File stubDescriptor, File outputDirectory, MavenProject mavenProject,
                         ArtifactRepository localRepository )
        throws DaemonGeneratorException;

    public Daemon loadModel( File stubDescriptor )
        throws DaemonGeneratorException;
}
