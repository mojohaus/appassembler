package org.codehaus.mojo.appassembler;

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
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.repository.layout.LegacyRepositoryLayout;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.appassembler.repository.FlatRepositoryLayout;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Util
{
    private static ArtifactRepositoryLayout defaultLayout = new DefaultRepositoryLayout();

    public static String getRelativePath( Artifact artifact, String layout )
    {
        ArtifactRepositoryLayout repositoryLayout;

        try
        {
            repositoryLayout = getRepositoryLayout( layout );
        }
        catch ( MojoFailureException e )
        {
            repositoryLayout = defaultLayout;
        }

        return getRelativePath( artifact, repositoryLayout );
    }

    public static String getRelativePath( Artifact artifact, ArtifactRepositoryLayout layout )
    {
        return layout.pathOf( artifact );
    }

    public static ArtifactRepositoryLayout getRepositoryLayout( String layout )
        throws MojoFailureException
    {
        ArtifactRepositoryLayout repositoryLayout;

        if ( layout == null || layout.equals( "default" ) )
        {
            repositoryLayout = new DefaultRepositoryLayout();
        }
        else if ( layout.equals( "legacy" ) )
        {
            repositoryLayout = new LegacyRepositoryLayout();
        }
        else if ( layout.equals( "flat" ) )
        {
            repositoryLayout = new FlatRepositoryLayout();
        }
        else
        {
            throw new MojoFailureException( "Unknown repository layout '" + layout + "'." );
        }

        return repositoryLayout;
    }
}
