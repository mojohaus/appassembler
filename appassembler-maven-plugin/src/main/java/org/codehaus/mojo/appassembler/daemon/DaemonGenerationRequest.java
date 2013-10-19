package org.codehaus.mojo.appassembler.daemon;

/*
 * The MIT License
 *
 * Copyright (c) 2006-2012, The Codehaus
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

import java.io.File;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.model.Daemon;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DaemonGenerationRequest
{
    private String platform;

    private File stubDescriptor;

    // TODO: what is the difference?
    private Daemon stubDaemon;

    private Daemon daemon;

    private File outputDirectory;

    private String binFolder;

    private MavenProject mavenProject;

    private ArtifactRepository localRepository;

    private String outputFileNameMapping;

    private ArtifactRepositoryLayout repositoryLayout = new DefaultRepositoryLayout();

    /**
     * The constructor for the request.
     */
    public DaemonGenerationRequest()
    {
    }

    /**
     * Request with the given parameters.
     * 
     * @param daemon The Daemon to use.
     * @param project The Maven Project
     * @param localRepository The local repository.
     * @param outputDir The output directory.
     * @param binFolder The binary folder.
     */
    public DaemonGenerationRequest( Daemon daemon, MavenProject project, ArtifactRepository localRepository,
                                    File outputDir, String binFolder )
    {
        this.daemon = daemon;

        this.mavenProject = project;

        this.localRepository = localRepository;

        this.outputDirectory = outputDir;

        this.binFolder = binFolder;
    }

    /**
     * Get the Plaform.
     * 
     * @return the Platform.
     */
    public String getPlatform()
    {
        return platform;
    }

    /**
     * @param platform Set the platform.
     */
    public void setPlatform( String platform )
    {
        this.platform = platform;
    }

    /**
     * Get the StubDescriptor FIXME: What for is this needed?
     * 
     * @return The Stub Descriptor file.
     */
    public File getStubDescriptor()
    {
        return stubDescriptor;
    }

    /**
     * Set the StubDescriptor. FIXME: What for is this needed?
     * 
     * @param stubDescriptor The File instance for the descriptor.
     */
    public void setStubDescriptor( File stubDescriptor )
    {
        this.stubDescriptor = stubDescriptor;
    }

    /**
     * Get the StubDaemon FIXME: Is this needed?
     * 
     * @return The set stub Daemon
     */
    public Daemon getStubDaemon()
    {
        return stubDaemon;
    }

    /**
     * Set the StubDaemon. FIXME: Is this needed?
     * 
     * @param stubDaemon This will be set.
     */
    public void setStubDaemon( Daemon stubDaemon )
    {
        this.stubDaemon = stubDaemon;
    }

    /**
     * Get the Daemon of the current request.
     * 
     * @return The Daemon instance.
     */
    public Daemon getDaemon()
    {
        return daemon;
    }

    /**
     * Set the daemon.
     * 
     * @param daemon Instance of a Daemon.
     */
    public void setDaemon( Daemon daemon )
    {
        this.daemon = daemon;
    }

    /**
     * Get the current outputDirectory.
     * 
     * @return File instance of the current outputDirectory.
     */
    public File getOutputDirectory()
    {
        return outputDirectory;
    }

    /**
     * Set the current output directory.
     * 
     * @param outputDirectory The output directory as a File.
     */
    public void setOutputDirectory( File outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Get the use MavenProject.
     * 
     * @return MavenProject instance.
     */
    public MavenProject getMavenProject()
    {
        return mavenProject;
    }

    /**
     * Set the Maven Project.
     * 
     * @param mavenProject instance to be set.
     */
    public void setMavenProject( MavenProject mavenProject )
    {
        this.mavenProject = mavenProject;
    }

    /**
     * Get the local repository.
     * 
     * @return Instance of the ArtifactRepository.
     */
    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }

    /**
     * @param localRepository Set the local repositoy.
     */
    public void setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;
    }

    /**
     * @return The current repository layout.
     */
    public ArtifactRepositoryLayout getRepositoryLayout()
    {
        return repositoryLayout;
    }

    /**
     * Set the current repository layout.
     * 
     * @param repositoryLayout The repositoryLayout which will be set.
     */
    public void setRepositoryLayout( ArtifactRepositoryLayout repositoryLayout )
    {
        this.repositoryLayout = repositoryLayout;
    }

    /**
     * Get the current binary folder.
     * 
     * @return the setting of the binary folder.
     */
    public String getBinFolder()
    {
        return binFolder;
    }

    /**
     * Set the binary folder.
     * 
     * @param binFolder The folder.
     */
    public void setBinFolder( String binFolder )
    {
        this.binFolder = binFolder;
    }

    /**
     * Get the output file name mapping.
     * 
     * @return The mapping
     */
    public String getOutputFileNameMapping()
    {
        return outputFileNameMapping;
    }

    /**
     * Set the output file name mapping.
     * 
     * @param outputFileNameMapping The mapping
     */
    public void setOutputFileNameMapping( String outputFileNameMapping )
    {
        this.outputFileNameMapping = outputFileNameMapping;
    }
}
