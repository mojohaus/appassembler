package org.codehaus.mojo.appassembler.util;

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
     * get relative path the copied artifact using base version. This is mainly use to SNAPSHOT instead of timestamp in
     * the file name
     * 
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
