package org.codehaus.mojo.appassembler.daemon;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Util
{
    private static ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();

    public static String getAbsolutePath( Artifact artifact )
    {
        return layout.pathOf( artifact );
    }
    
    public static String getRelativePath( Artifact artifact )
    {
        String versionNumber = artifact.getVersion();
        
        String groupIdPart = artifact.getGroupId().replace( '.', '/');
        String artifactIdPart = artifact.getArtifactId();
        
        String relativePath = groupIdPart + "/" + artifactIdPart + "/" + versionNumber + "/" + artifactIdPart + "-" +
            versionNumber + ".jar";

        return relativePath;
    }
    
}
