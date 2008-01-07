package org.codehaus.mojo.appassembler.util;

/*
 * The MIT License
 *
 * Copyright 2005-2008 The Codehaus.
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

import junit.framework.TestCase;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringOutputStream;

import java.io.IOException;
import java.io.InputStream;

public class FormattedPropertiesTest
    extends TestCase
{
    private FormattedProperties formattedProperties;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        formattedProperties = new FormattedProperties();

        formattedProperties.read(
            getClass().getResourceAsStream( "/org/codehaus/mojo/appassembler/util/test.properties" ) );
    }

    private void saveAndCompare( String expectedResource )
        throws IOException
    {
        StringOutputStream string = new StringOutputStream();
        formattedProperties.save( string );

        StringOutputStream expected = new StringOutputStream();
        InputStream asStream = getClass().getResourceAsStream( expectedResource );
        try
        {
            IOUtil.copy( asStream, expected );
        }
        finally
        {
            IOUtil.close( asStream );
        }

        assertEquals( expected.toString(), string.toString() );
    }

    public void testReadingProperties()
    {
        assertEquals( "original.value", formattedProperties.getProperty( "changing.property" ) );

        assertNull( formattedProperties.getProperty( "adding.property" ) );

        assertEquals( "original.value", formattedProperties.getProperty( "removing.property" ) );

        assertEquals( "list.item.1", formattedProperties.getProperty( "adding.property.to.list.1" ) );
        assertNull( formattedProperties.getProperty( "adding.property.to.list.2" ) );

        assertEquals( "list.item.1", formattedProperties.getProperty( "adding.property.to.long.list.1" ) );
        assertEquals( "list.item.2", formattedProperties.getProperty( "adding.property.to.long.list.2" ) );
        assertEquals( "list.item.3", formattedProperties.getProperty( "adding.property.to.long.list.3" ) );
        assertNull( formattedProperties.getProperty( "adding.property.to.long.list.4" ) );

        assertNull( formattedProperties.getProperty( "adding.property.to.empty.list.1" ) );

        assertNull( formattedProperties.getProperty( "adding.property.to.commented.list.1" ) );
        assertNull( formattedProperties.getProperty( "adding.property.to.commented.list.2" ) );

        assertNull( formattedProperties.getProperty( "removing.property.from.commented.list.1" ) );

        assertNull( formattedProperties.getProperty( "adding.property.to.long.commented.list.1" ) );
        assertNull( formattedProperties.getProperty( "adding.property.to.long.commented.list.2" ) );
        assertNull( formattedProperties.getProperty( "adding.property.to.long.commented.list.3" ) );

        assertEquals( "list.item.1",
                      formattedProperties.getProperty( "adding.property.to.existing.commented.list.1" ) );
        assertNull( formattedProperties.getProperty( "adding.property.to.existing.commented.list.2" ) );

        assertEquals( "list.item.1",
                      formattedProperties.getProperty( "removing.property.from.existing.commented.list.1" ) );
        assertNull( formattedProperties.getProperty( "removing.property.from.existing.commented.list.2" ) );
    }

    public void testChangingProperty()
        throws IOException
    {
        formattedProperties.setProperty( "changing.property", "new.value" );

        saveAndCompare( "/org/codehaus/mojo/appassembler/util/changing-property.properties" );
    }

    public void testAddingProperty()
        throws IOException
    {
        formattedProperties.setProperty( "adding.property", "new.value" );

        saveAndCompare( "/org/codehaus/mojo/appassembler/util/adding-property.properties" );
    }

    public void testRemovingProperty()
        throws IOException
    {
        formattedProperties.removeProperty( "removing.property" );

        saveAndCompare( "/org/codehaus/mojo/appassembler/util/removing-property.properties" );
    }

    public void testAddingPropertyToList()
        throws IOException
    {
        formattedProperties.setProperty( "adding.property.to.list.2", "list.item.2" );

        saveAndCompare( "/org/codehaus/mojo/appassembler/util/adding-property-to-list.properties" );
    }

    public void testAddingPropertyToLongList()
        throws IOException
    {
        formattedProperties.setProperty( "adding.property.to.long.list.4", "list.item.4" );

        saveAndCompare( "/org/codehaus/mojo/appassembler/util/adding-property-to-long-list.properties" );
    }

    public void testAddingPropertyToEmptyList()
        throws IOException
    {
        formattedProperties.setProperty( "adding.property.to.empty.list.1", "list.item.1" );

        saveAndCompare( "/org/codehaus/mojo/appassembler/util/adding-property-to-empty-list.properties" );
    }

    public void testAddingPropertyToListWithCommentedExample()
        throws IOException
    {
        formattedProperties.setProperty( "adding.property.to.commented.list.1", "list.item.1" );

        saveAndCompare( "/org/codehaus/mojo/appassembler/util/adding-property-to-commented-list.properties" );
    }

    public void testRemovingPropertyFromListWithCommentedExample()
        throws IOException
    {
        formattedProperties.removeProperty( "removing.property.from.commented.list.1" );

        // unchanged
        saveAndCompare( "/org/codehaus/mojo/appassembler/util/test.properties" );
    }

    public void testAddingPropertyToListWithLongCommentedExample()
        throws IOException
    {
        formattedProperties.setProperty( "adding.property.to.long.commented.list.1", "list.item.1" );
        formattedProperties.setProperty( "adding.property.to.long.commented.list.2", "list.item.2" );

        saveAndCompare( "/org/codehaus/mojo/appassembler/util/adding-property-to-long-commented-list.properties" );
    }

    public void testAddingPropertyToExistingListWithCommentedExample()
        throws IOException
    {
        formattedProperties.setProperty( "adding.property.to.existing.commented.list.2", "list.item.2" );

        saveAndCompare( "/org/codehaus/mojo/appassembler/util/adding-property-to-existing-commented-list.properties" );
    }

    public void testRemovingPropertyFromExistListWithCommentedExample()
        throws IOException
    {
        formattedProperties.removeProperty( "removing.property.from.existing.commented.list.1" );

        saveAndCompare(
            "/org/codehaus/mojo/appassembler/util/removing-property-from-existing-commented-list.properties" );
    }

    public void testAddingPropertyAfterAnother()
        throws IOException
    {
        formattedProperties.setPropertyAfter( "adding.property.after.another", "new.value", "changing.property" );

        saveAndCompare( "/org/codehaus/mojo/appassembler/util/adding-property-after-another.properties" );
    }
}
