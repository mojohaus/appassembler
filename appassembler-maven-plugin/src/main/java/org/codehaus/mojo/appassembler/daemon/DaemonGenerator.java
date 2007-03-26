package org.codehaus.mojo.appassembler.daemon;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.model.Daemon;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface DaemonGenerator
{
    String ROLE = DaemonGenerator.class.getName();

    void generate( DaemonGenerationRequest generationRequest )
        throws DaemonGeneratorException;
}
