package org.codehaus.mojo.appassembler.daemon;

/*
 * The MIT License
 *
 * Copyright 2005-2007 The Codehaus.
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

    private MavenProject mavenProject;

    private ArtifactRepository localRepository;

    private ArtifactRepositoryLayout repositoryLayout = new DefaultRepositoryLayout();


    public DaemonGenerationRequest()
    {
    }

    public DaemonGenerationRequest( Daemon daemon, MavenProject project, ArtifactRepository localRepository,
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

    public void setRepositoryLayout( ArtifactRepositoryLayout repositoryLayout )
    {
        this.repositoryLayout = repositoryLayout;
    }

}
