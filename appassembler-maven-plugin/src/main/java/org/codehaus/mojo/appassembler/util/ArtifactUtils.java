package org.codehaus.mojo.appassembler.util;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.codehaus.plexus.util.StringUtils;

public final class ArtifactUtils
{

    private ArtifactUtils()
    {
    }

    /**
     * get relative path the copied artifact using base version.
     * This is mainly use to SNAPSHOT instead of timestamp in the file name
     * @param artifactRepositoryLayout
     * @param artifact
     * @return
     */
    public static String pathBaseVersionOf( ArtifactRepositoryLayout artifactRepositoryLayout, Artifact artifact )
    {
        ArtifactHandler artifactHandler = artifact.getArtifactHandler();

        StringBuffer fileName = new StringBuffer();

        fileName.append( artifact.getArtifactId() ).append( "-" ).append( artifact.getBaseVersion() );

        if ( artifact.hasClassifier() )
        {
            fileName.append( "-" ).append( artifact.getClassifier() );
        }

        if ( artifactHandler.getExtension() != null && artifactHandler.getExtension().length() > 0 )
        {
            fileName.append( "." ).append( artifactHandler.getExtension() );
        }

        String relativePath = artifactRepositoryLayout.pathOf( artifact );
        String[] tokens = StringUtils.split( relativePath, "/" );
        tokens[tokens.length - 1] = fileName.toString();

        StringBuffer path = new StringBuffer();

        for ( int i = 0; i < tokens.length; ++i )
        {

            path.append( tokens[i] );
            if ( i != tokens.length - 1 )
            {
                path.append( "/" );
            }
        }

        return path.toString();

    }


}
