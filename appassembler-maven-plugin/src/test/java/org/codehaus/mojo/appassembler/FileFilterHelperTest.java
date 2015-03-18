package org.codehaus.mojo.appassembler;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.codehaus.mojo.appassembler.util.FileFilterHelper;
import org.codehaus.mojo.appassembler.util.TestBase;

public class FileFilterHelperTest
    extends TestCase
{

    /**
     * Just copied from FileUitlsTest.java (plexus-utils)
     *
     * @param size
     * @return
     */
    private byte[] generateTestData( final long size )
    {
        try
        {
            ByteArrayOutputStream baout = new ByteArrayOutputStream();
            generateTestData( baout, size );
            return baout.toByteArray();
        }
        catch ( IOException ioe )
        {
            throw new RuntimeException( "This should never happen: " + ioe.getMessage() );
        }
    }

    /**
     * Just copied from FileUitlsTest.java (plexus-utils)
     *
     * @param size
     * @return
     */
    private void generateTestData( final OutputStream out, final long size )
        throws IOException
    {
        for ( int i = 0; i < size; i++ )
        {
            // nice varied byte pattern compatible with Readers and Writers
            out.write( (byte) ( ( i % 127 ) + 1 ) );
        }
    }

    /**
     * Just copied from FileUitlsTest.java (plexus-utils)
     *
     * @param size
     * @return
     */
    private byte[] createFile( final File file, final long size )
        throws IOException
    {
        if ( !file.getParentFile().exists() )
        {
            throw new IOException( "Cannot create file " + file + " as the parent directory does not exist" );
        }

        byte[] data = generateTestData( size );

        final BufferedOutputStream output = new BufferedOutputStream( new FileOutputStream( file ) );

        try
        {
            output.write( data );

            return data;
        }
        finally
        {
            output.close();
        }
    }

    public void testCreateDefaultFilter()
        throws IOException
    {

        File testTargetDirectory = new File( TestBase.getTargetDir() );

        File sourceDirectory = new File( testTargetDirectory, "source-structure" );
        sourceDirectory.mkdirs();

        createFile( new File( sourceDirectory, ".cvsignore" ), 200 );
        createFile( new File( sourceDirectory, "ShouldBeIgnored.#" ), 100 );
        createFile( new File( sourceDirectory, "project.pj" ), 500 );

        createFile( new File( sourceDirectory, "README.md" ), 250 );
        createFile( new File( sourceDirectory, "test-1.2-SNAPSHOT.jar" ), 267 );

        File gitDirectory = new File( sourceDirectory, ".git" );
        gitDirectory.mkdirs();
        createFile( new File( gitDirectory, "a" ), 10 );
        createFile( new File( gitDirectory, "b" ), 100 );
        createFile( new File( gitDirectory, "c" ), 1000 );

        File subDirectory = new File( sourceDirectory, "subdir" );
        subDirectory.mkdirs();
        createFile( new File( subDirectory, "x.txt" ), 200 );
        createFile( new File( subDirectory, "README" ), 200 );
        createFile( new File( subDirectory, "pom.xml" ), 2000 );
        createFile( new File( subDirectory, "#ThisFileWillBeIgnored#" ), 2000 );

        File targetDirectory = new File( testTargetDirectory, "destination-structure" );
        if ( targetDirectory.exists() )
        {
            FileUtils.deleteDirectory( targetDirectory );
        }

        FileUtils.copyDirectory( sourceDirectory, targetDirectory, FileFilterHelper.createDefaultFilter() );

        assertTrue( targetDirectory.exists() );
        assertTrue( targetDirectory.isDirectory() );

        assertFalse( new File( targetDirectory, ".cvsignore" ).exists() );
        assertFalse( new File( targetDirectory, "ShouldBeIgnored.#" ).exists() );
        assertFalse( new File( targetDirectory, "project.pj" ).exists() );
        assertFalse( new File( targetDirectory, ".git" ).exists() );

        assertTrue( new File( targetDirectory, "README.md" ).exists() );
        assertTrue( new File( targetDirectory, "README.md" ).isFile() );
        assertTrue( new File( targetDirectory, "test-1.2-SNAPSHOT.jar" ).exists() );
        assertTrue( new File( targetDirectory, "test-1.2-SNAPSHOT.jar" ).isFile() );

        subDirectory = new File( targetDirectory, "subdir" );
        assertTrue( subDirectory.exists() );
        assertTrue( subDirectory.isDirectory() );

        assertTrue( new File( subDirectory, "x.txt" ).exists() );
        assertTrue( new File( subDirectory, "x.txt" ).isFile() );

        assertTrue( new File( subDirectory, "README" ).exists() );
        assertTrue( new File( subDirectory, "README" ).isFile() );

        assertTrue( new File( subDirectory, "pom.xml" ).exists() );
        assertTrue( new File( subDirectory, "pom.xml" ).isFile() );

        assertFalse( new File( subDirectory, "#ThisFileWillBeIgnored#" ).exists() );

    }

}
