package org.codehaus.mojo.appassembler.daemon.generic;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerator;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.daemon.merge.DaemonMerger;
import org.codehaus.mojo.appassembler.model.Classpath;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.mojo.appassembler.model.io.stax.AppassemblerModelStaxWriter;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role-hint="generic"
 */
public class GenericDaemonGenerator extends AbstractLogEnabled implements DaemonGenerator
{
    /**
     * @plexus.requirement
     */
    private DaemonMerger daemonMerger;

    // -----------------------------------------------------------------------
    // DaemonGenerator Implementation
    // -----------------------------------------------------------------------

    public void generate( DaemonGenerationRequest request ) throws DaemonGeneratorException
    {
        // -----------------------------------------------------------------------
        // Create the daemon from the Maven project
        // -----------------------------------------------------------------------

        Daemon createdDaemon = createDaemon( request.getMavenProject(), request.getRepositoryLayout() );

        // -----------------------------------------------------------------------
        // Merge the given stub daemon and the generated
        // -----------------------------------------------------------------------

        Daemon mergedDaemon = daemonMerger.mergeDaemons( request.getDaemon(), createdDaemon );

        // -----------------------------------------------------------------------
        // Write out the project
        // -----------------------------------------------------------------------

        OutputStreamWriter writer = null;

        try
        {

            FileUtils.forceMkdir( request.getOutputDirectory() );

            File outputFile = new File( request.getOutputDirectory(), mergedDaemon.getId() + ".xml" );

            FileOutputStream fos = new FileOutputStream( outputFile );

            writer = new OutputStreamWriter( fos, "UTF-8" );

            AppassemblerModelStaxWriter staxWriter = new AppassemblerModelStaxWriter();
            staxWriter.write( writer, mergedDaemon );
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Error while writing output file: " + request.getOutputDirectory(), e );
        }
        catch ( XMLStreamException e )
        {
            throw new DaemonGeneratorException( "Error while writing output file: " + request.getOutputDirectory(), e );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private Daemon createDaemon( MavenProject project, ArtifactRepositoryLayout layout )
    {
        Daemon complete = new Daemon();

        complete.setClasspath( new Classpath() );

        // -----------------------------------------------------------------------
        // Add the project itself as a dependency.
        // -----------------------------------------------------------------------
        Dependency projectDependency = new Dependency();
        Artifact projectArtifact = project.getArtifact();
        projectArtifact.isSnapshot();
        projectDependency.setGroupId( projectArtifact.getGroupId() );
        projectDependency.setArtifactId( projectArtifact.getArtifactId() );
        projectDependency.setVersion( projectArtifact.getVersion() );
        projectDependency.setClassifier( projectArtifact.getClassifier() );
        projectDependency.setRelativePath( layout.pathOf( projectArtifact ) );
        complete.getClasspath().addDependency( projectDependency );

        // -----------------------------------------------------------------------
        // Add all the dependencies from the project.
        // -----------------------------------------------------------------------
        for ( Iterator it = project.getRuntimeArtifacts().iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();
            
            artifact.isSnapshot();

            Dependency dependency = new Dependency();
            dependency.setGroupId( artifact.getGroupId() );
            dependency.setArtifactId( artifact.getArtifactId() );
            dependency.setVersion( artifact.getVersion() );

            dependency.setClassifier( artifact.getClassifier() );
            dependency.setRelativePath( layout.pathOf( artifact ) );

            complete.getClasspath().addDependency( dependency );
        }

        return complete;
    }
}
