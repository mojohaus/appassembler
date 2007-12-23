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

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.model.Daemon;

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

    void generateDaemon( DaemonGenerationRequest generationRequest )
        throws DaemonGeneratorException;

    Daemon mergeDaemons( Daemon dominant, Daemon recessive )
        throws DaemonGeneratorException;

    Daemon loadModel( File stubDescriptor )
        throws DaemonGeneratorException;

    /**
     * @param daemon     The daemon to validate
     * @param descriptor An optional file reference that will be used in the exception messages.
     * @throws DaemonGeneratorException
     */
    void validateDaemon( Daemon daemon, File descriptor )
        throws DaemonGeneratorException;
}
