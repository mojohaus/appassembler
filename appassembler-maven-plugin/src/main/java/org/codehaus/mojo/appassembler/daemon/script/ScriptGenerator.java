package org.codehaus.mojo.appassembler.daemon.script;

import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.repository.ArtifactRepository;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface ScriptGenerator
{
    String ROLE = ScriptGenerator.class.getName();

    public void createBinScript( String platform,
                                 Daemon daemon,
                                 File outputDirectory )
        throws DaemonGeneratorException;
}
