package org.codehaus.mojo.appassembler.daemon;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.model.Daemon;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DaemonGenerationRequest
{
    private String platform;

    private File stubDescriptor;

    private Daemon stubDaemon;

    private Daemon daemon;

    private File outputDirectory;

    private MavenProject mavenProject;

    private ArtifactRepository localRepository;

    private ArtifactRepositoryLayout repositoryLayout = new DefaultRepositoryLayout();

    /**
     * A path to the repository relative from the root directory of the deamon.
     */
    private String repositoryPath;


    public DaemonGenerationRequest()
    {
    }

    public DaemonGenerationRequest( Daemon daemon,
                                    MavenProject project,
                                    ArtifactRepository localRepository,
                                    File outputDir )
    {
        this.daemon = daemon;

        this.mavenProject = project;

        this.localRepository = localRepository;

        this.outputDirectory = outputDir;
    }

    public String getPlatform()
    {
        return platform;
    }

    public void setPlatform( String platform )
    {
        this.platform = platform;
    }

    public File getStubDescriptor()
    {
        return stubDescriptor;
    }

    public void setStubDescriptor( File stubDescriptor )
    {
        this.stubDescriptor = stubDescriptor;
    }

    public Daemon getStubDaemon()
    {
        return stubDaemon;
    }

    public void setStubDaemon( Daemon stubDaemon )
    {
        this.stubDaemon = stubDaemon;
    }

    public Daemon getDaemon()
    {
        return daemon;
    }

    public void setDaemon( Daemon daemon )
    {
        this.daemon = daemon;
    }

    public File getOutputDirectory()
    {
        return outputDirectory;
    }

    public void setOutputDirectory( File outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }

    public MavenProject getMavenProject()
    {
        return mavenProject;
    }

    public void setMavenProject( MavenProject mavenProject )
    {
        this.mavenProject = mavenProject;
    }

    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }

    public void setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;
    }

    public ArtifactRepositoryLayout getRepositoryLayout()
    {
        return repositoryLayout;
    }

    public void setRepositoryLayout(ArtifactRepositoryLayout repositoryLayout)
    {
        this.repositoryLayout = repositoryLayout;
    }

    public String getRepositoryPath()
    {
        return repositoryPath;
    }

    public void setRepositoryPath( String repositoryPath )
    {
        this.repositoryPath = repositoryPath;
    }
}
