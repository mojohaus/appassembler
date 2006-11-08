package org.codehaus.mojo.appassembler.daemon;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.mojo.appassembler.model.generic.Daemon;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class GenericDaemonGeneratorTest
    extends AbstractDaemonGeneratorTest

{
    protected String getGeneratorId()
    {
        return "generic";
    }

    protected String getPOM()
    {
        return "src/test/resources/project-1/pom.xml";
    }

    protected String getDescriptor()
    {
        return "src/test/resources/project-1/descriptor.xml";
    }

    protected String getOutputDir()
    {
        return "target/output-1-generic";
    }
}
