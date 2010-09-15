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

import org.codehaus.plexus.util.IOUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to read/write a properties file, and retain the formatting through modifications.
 */
public class FormattedProperties
{
    private static final Pattern LIST_KEY_PATTERN = Pattern.compile( "^(.*)\\.[0-9]+$" );

    /**
     * The properties delegate.
     */
    private final Properties properties = new Properties();

    /**
     * The last line where a given property was encountered.
     */
    private Map propertyLines;

    /**
     * The actual lines of the file for writing back as it was.
     */
    private List fileLines;

    /**
     * Keeping track of properties that are lists.
     */
    private Map listProperties = new HashMap();

    /**
     * A map of property chains to add properties after.
     */
    private Map afterProperties = new HashMap();

    public void setProperty( String key, String value )
    {
        synchronized ( properties )
        {
            properties.setProperty( key, value );

            // does the property look like a list (ends in .X where X is an integer)?
            Matcher m = LIST_KEY_PATTERN.matcher( key );
            if ( m.matches() )
            {
                String listKey = m.group( 1 );

                // add the property to a list keyed by the base key of the list
                List p = (List) listProperties.get( listKey );
                if ( p == null )
                {
                    p = new ArrayList();
                    listProperties.put( listKey, p );
                }
                p.add( key );
            }
        }
    }

    public String getProperty( String key )
    {
        synchronized ( properties )
        {
            return properties.getProperty( key );
        }
    }

    public String getProperty( String key, String defaultValue )
    {
        synchronized ( properties )
        {
            return properties.getProperty( key, defaultValue );
        }
    }

    public void removeProperty( String key )
    {
        synchronized ( properties )
        {
            properties.remove( key );
        }
    }

    /**
     * Read in the properties from the given stream. Note that this will be used as the basis of the next formatted
     * write, even though properties from any previous read are still retained. This allows adding properties to the top
     * of the file.
     *
     * @param inputStream the stream to read from
     * @throws IOException if there is a problem reading the stream
     */
    public void read( InputStream inputStream )
        throws IOException
    {
        synchronized ( properties )
        {
            fileLines = new ArrayList();
            propertyLines = new HashMap();

            BufferedReader r = new BufferedReader( new InputStreamReader( inputStream ) );

            try
            {
                int lineNo = 1;
                String line = r.readLine();
                while ( line != null )
                {
                    // parse the key and value. No = means it's not a property, multiple = will be attributed to the
                    // value
                    String[] pair = line.split( "=", 2 );
                    String key = pair[0];
                    String value;
                    if ( pair.length > 1 )
                    {
                        value = pair[1].trim();

                        // is the line a comment?
                        boolean commented = false;
                        if ( key.startsWith( "#" ) )
                        {
                            commented = true;
                            key = key.substring( 1 );
                        }

                        key = key.trim();

                        // if it's not commented, set the property
                        if ( !commented )
                        {
                            // must use our setProperty to update list properties too
                            setProperty( key, value );
                        }

                        // regardless of whether it's a comment, track the key it might have been (only the base key if
                        // it's a list) so we know where to add any new properties later
                        Matcher m = LIST_KEY_PATTERN.matcher( key );
                        if ( m.matches() )
                        {
                            key = m.group( 1 );
                        }

                        propertyLines.put( key, new Integer( lineNo ) );
                    }

                    fileLines.add( line );

                    line = r.readLine();
                    lineNo++;
                }
            }
            finally
            {
                IOUtil.close( r );
            }
        }
    }

    public void save( OutputStream outputStream )
    {
        synchronized ( properties )
        {
            PrintWriter writer = new PrintWriter( new OutputStreamWriter( outputStream ) );

            // TODO: we should be updating the fileLines and propertyLines as a result of this too
            try
            {
                Set writtenProperties = new HashSet();

                // tracking the old file lines, we'll just add ours in as we go
                for ( int i = 0; i < fileLines.size(); i++ )
                {
                    String line = (String) fileLines.get( i );

                    // skip processing empty lines (though they are written later)
                    if ( line.trim().length() > 0 )
                    {
                        String[] pair = line.split( "=", 2 );
                        String key = pair[0];
                        if ( key.startsWith( "#" ) )
                        {
                            // comments are written back out verbatim. If we match the key, we'll write the value below
                            // it (unless there is a later instance in a list)
                            key = key.substring( 1 );
                            writer.println( line );
                        }

                        key = key.trim();

                        // look for an exact match on the key to replace on the current line
                        if ( new Integer( i + 1 ).equals( propertyLines.get( key ) ) )
                        {
                            String value = properties.getProperty( key );
                            if ( value != null )
                            {
                                writer.println( key + "=" + value );
                                writtenProperties.add( key );
                            }
                        }

                        // look for chained properties to add
                        if ( afterProperties.containsKey( key ) )
                        {
                            List p = (List) afterProperties.get( key );
                            for ( Iterator j = p.iterator(); j.hasNext(); )
                            {
                                String pKey = (String) j.next();

                                String value = properties.getProperty( pKey );
                                if ( value != null && !writtenProperties.contains( pKey ) )
                                {
                                    writer.println( pKey + "=" + value );
                                    writtenProperties.add( pKey );
                                }
                            }
                        }

                        // check if we matched the last key in a list, and if so write all unwritten list properties
                        Matcher m = LIST_KEY_PATTERN.matcher( key );
                        if ( m.matches() )
                        {
                            key = m.group( 1 );

                            if ( new Integer( i + 1 ).equals( propertyLines.get( key ) ) )
                            {
                                List p = (List) listProperties.get( key );
                                if ( p != null )
                                {
                                    for ( Iterator j = p.iterator(); j.hasNext(); )
                                    {
                                        String itemKey = (String) j.next();

                                        if ( !writtenProperties.contains( itemKey ) )
                                        {
                                            String value = properties.getProperty( itemKey );
                                            if ( value != null )
                                            {
                                                writer.println( itemKey + "=" + value );
                                                writtenProperties.add( itemKey );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        writer.println( line );
                    }
                }

                for ( Iterator i = properties.keySet().iterator(); i.hasNext(); )
                {
                    String key = (String) i.next();
                    if ( !writtenProperties.contains( key ) )
                    {
                        String value = properties.getProperty( key );
                        if ( value != null )
                        {
                            writer.println( key + "=" + value );
                        }
                    }
                }
            }
            finally
            {
                IOUtil.close( writer );
            }
        }
    }

    public void setPropertyAfter( String key, String value, String afterProperty )
    {
        List p = (List) afterProperties.get( afterProperty );
        if ( p == null )
        {
            p = new ArrayList();
            afterProperties.put( afterProperty, p );
        }
        p.add( key );

        setProperty( key, value );
    }
}
