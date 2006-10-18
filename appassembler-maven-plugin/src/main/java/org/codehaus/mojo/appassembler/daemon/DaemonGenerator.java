package org.codehaus.mojo.appassembler.daemon;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.model.generic.Daemon;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface DaemonGenerator
{
    String ROLE = DaemonGenerator.class.getName();

    void generate( Daemon descriptor, MavenProject project, ArtifactRepository localRepository, File outputDirectory )
        throws DaemonGeneratorException;
}
