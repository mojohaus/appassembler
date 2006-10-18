package org.codehaus.mojo.appassembler.daemon;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Util
{
    private static ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();

    public static String getRelativePath( Artifact artifact )
    {
        return layout.pathOf( artifact );
    }
}
