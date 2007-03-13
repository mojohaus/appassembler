package org.codehaus.mojo.appassembler.model.io;

import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.mojo.appassembler.model.Directory;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component
 */
public class DaemonModelUtil
{
    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static void storeModel( Daemon daemon, File outputFile )
        throws IOException
    {
        FileOutputStream outputStream = null;

        try
        {
            Document dom = createDocument( daemon );

            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();

            outputStream = new FileOutputStream( outputFile );

            transformer.transform( new DOMSource( dom ), new StreamResult( outputStream ) );
        }
        catch ( TransformerConfigurationException e )
        {
            throw new DaemonModelUtilException( "Error while creating transformer.", e );
        }
        catch ( TransformerException e )
        {
            throw new DaemonModelUtilException( "Error while transforming model.", e );
        }
        finally
        {
            close( outputStream );
        }
    }

    public static Daemon loadModel( File file )
        throws IOException
    {
        InputStream fileInputStream = null;

        try
        {
            fileInputStream = new FileInputStream( file );

            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            DaemonContentHandler daemonContentHandler = new DaemonContentHandler();
            xmlReader.setContentHandler( daemonContentHandler );
            xmlReader.parse( new InputSource( fileInputStream ) );

            return daemonContentHandler.getDaemon();
        }
        catch ( final SAXException e )
        {
            throw new DaemonModelUtilException( "Error while parsing '" + file + "'", e );
        }
        catch ( FileNotFoundException e )
        {
            throw new DaemonModelUtilException( "Could not find file: '" + file + "'" );
        }
        finally
        {
            close( fileInputStream );
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private static class DaemonModelUtilException
        extends IOException
    {
        public DaemonModelUtilException( String s )
        {
            super( s );
        }

        public DaemonModelUtilException( String s, Exception e )
        {
            super( s );

            initCause( e );
        }
    }

    private static class DaemonContentHandler
        extends DefaultHandler
    {
        private Daemon daemon;

        private List classpathElements;

        private Dependency dependency;

        private Directory directory;

        private JvmSettings jvmSettings;

        private List systemProperties;

        private boolean first = true;

        private boolean insideDaemon;

        private boolean insideClasspath;

        private boolean insideJvmSettings;

        private boolean insideDependency;

        private boolean insideDirectory;

        private boolean insideSystemProperties;

        private boolean insideSystemProperty;

        private String text;

        // -----------------------------------------------------------------------
        // ContentHandler Implementation
        // -----------------------------------------------------------------------

        public void startElement( String uri, String localName, String qName, Attributes attributes )
            throws SAXException
        {
            if ( first && !localName.equals( "daemon" ) )
            {
                throw new SAXException( "Illegal start tag '" + localName + "', expected 'daemon'." );
            }

            first = false;

            if ( localName.equals( "daemon" ) )
            {
                daemon = new Daemon();

                insideDaemon = true;
            }
            else if ( insideDaemon )
            {
                if ( localName.equals( "classpath" ) )
                {
                    insideClasspath = true;

                    classpathElements = new ArrayList();
                }
                else if ( localName.equals( "jvmSettings" ) )
                {
                    insideJvmSettings = true;

                    jvmSettings = new JvmSettings();
                }
                else if ( insideClasspath )
                {
                    if ( localName.equals( "dependency" ) )
                    {
                        insideDependency = true;

                        dependency = new Dependency();
                    }
                    else if ( localName.equals( "directory" ) )
                    {
                        insideDirectory = true;

                        directory = new Directory();
                    }
                }
                else if ( insideJvmSettings )
                {
                    if ( localName.equals( "systemProperties" ) )
                    {
                        insideSystemProperties = true;

                        systemProperties = new ArrayList();
                    }
                    else if ( insideSystemProperties )
                    {
                        if ( localName.equals( "systemProperty" ) )
                        {
                            insideSystemProperty = true;
                        }
                    }
                }
            }
        }

        public void characters( char ch[], int start, int length )
            throws SAXException
        {
            text = new String( ch, start, length );
        }

        public void endElement( String uri, String localName, String qName )
            throws SAXException
        {
            if ( localName.equals( "daemon" ) )
            {
                insideDaemon = false;
            }

            if ( insideDaemon )
            {
                if ( localName.equals( "id" ) )
                {
                    daemon.setId( text );
                }
                else if ( localName.equals( "mainClass" ) )
                {
                    daemon.setMainClass( text );
                }
                else if ( localName.equals( "classpath" ) )
                {
                    insideClasspath = false;

                    daemon.setClasspath( classpathElements );
                }
                else if ( insideClasspath )
                {
                    if ( localName.equals( "dependency" ) )
                    {
                        insideDependency = false;

                        classpathElements.add( dependency );

                        dependency = null;
                    }
                    else if ( localName.equals( "directory" ) )
                    {
                        insideDirectory = false;

                        classpathElements.add( directory );

                        directory = null;
                    }
                    else if ( insideDependency )
                    {
                        if ( localName.equals( "groupId" ) )
                        {
                            dependency.setGroupId( text );
                        }
                        else if ( localName.equals( "artifactId" ) )
                        {
                            dependency.setArtifactId( text );
                        }
                        else if ( localName.equals( "version" ) )
                        {
                            dependency.setVersion( text );
                        }
                        else if ( localName.equals( "relativePath" ) )
                        {
                            dependency.setRelativePath( text );
                        }
                    }
                    else if ( insideDirectory )
                    {
                        if ( localName.equals( "relativePath" ) )
                        {
                            directory.setRelativePath( text );
                        }
                    }
                }
                else if ( insideJvmSettings )
                {
                    if ( localName.equals( "jvmSettings" ) )
                    {
                        daemon.setJvmSettings( jvmSettings );

                        insideJvmSettings = false;
                    }
                    else if ( insideJvmSettings )
                    {
                        if ( localName.equals( "initialMemorySize" ) )
                        {
                            jvmSettings.setInitialMemorySize( text );
                        }
                        else if ( localName.equals( "maxMemorySize" ) )
                        {
                            jvmSettings.setMaxMemorySize( text );
                        }
                        else if ( localName.equals( "maxStackSize" ) )
                        {
                            jvmSettings.setMaxStackSize( text );
                        }
                        else if ( localName.equals( "systemProperties" ) )
                        {
                            insideSystemProperties = false;

                            jvmSettings.setSystemProperties( systemProperties );
                        }
                        else if ( insideSystemProperty )
                        {
                            systemProperties.add( text );

                            insideSystemProperty = false;
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------------
        //
        // -----------------------------------------------------------------------

        public Daemon getDaemon()
        {
            return daemon;
        }
    }

    private static Document createDocument( Daemon d )
        throws DaemonModelUtilException
    {
        Document document = createEmptyDocument();

        Element daemon = document.createElement( "daemon" );
        document.appendChild( daemon );

        addSimpleElement( daemon, "id", d.getId() );
        addSimpleElement( daemon, "mainClass", d.getMainClass() );
        daemon.appendChild( createClasspath( document.createElement( "classpath" ), d.getClasspath() ) );
        daemon.appendChild( createJvmSettings( document.createElement( "jvmSettings" ), d.getJvmSettings() ) );

        return document;
    }

    private static Node createClasspath( Element element, List classpath )
        throws DaemonModelUtilException
    {
        for ( Iterator it = classpath.iterator(); it.hasNext(); )
        {
            Object o =  it.next();

            Element e;

            if ( o instanceof Dependency )
            {
                Dependency dependency = (Dependency) o;

                e = element.getOwnerDocument().createElement( "dependency" );

                addSimpleElement( e, "groupId", dependency.getGroupId() );
                addSimpleElement( e, "artifactId", dependency.getArtifactId() );
                addSimpleElement( e, "version", dependency.getVersion() );
                addSimpleElement( e, "relativePath", dependency.getRelativePath() );
            }
            else if ( o instanceof Directory )
            {
                Directory directory = (Directory) o;

                e = element.getOwnerDocument().createElement( "directory" );

                addSimpleElement( e, "relativePath", directory.getRelativePath() );
            }
            else
            {
                throw new DaemonModelUtilException( "Unknonwn classpath element type '" + o.getClass().getName() + "'." );
            }

            element.appendChild( e );
        }

        return element;
    }

    private static Node createJvmSettings( Element element, JvmSettings jvmSettings )
        throws DaemonModelUtilException
    {
        addSimpleElement( element, "initialMemorySize", jvmSettings.getInitialMemorySize() );
        addSimpleElement( element, "maxMemorySize", jvmSettings.getMaxMemorySize() );
        addSimpleElement( element, "maxStackSize", jvmSettings.getMaxStackSize() );
        element.appendChild( createSystemProperties( element.getOwnerDocument().createElement( "systemProperties" ),
                                                     jvmSettings.getSystemProperties() ) );

        return element;
    }

    private static Node createSystemProperties( Element element, List systemProperties )
        throws DaemonModelUtilException
    {
        for ( Iterator it = systemProperties.iterator(); it.hasNext(); )
        {
            Object o = it.next();

            if ( o instanceof String )
            {
                addSimpleElement( element, "systemProperty", o.toString() );
            }
            else
            {
                throw new DaemonModelUtilException( "Unknonwn system property element type '" + o.getClass().getName() + "'." );
            }
        }

        return element;
    }

    private static void addSimpleElement( Element parent, String elementName, String value )
    {
        Element element = parent.getOwnerDocument().createElement( elementName );

        parent.appendChild( element );

        element.setTextContent( value );
    }

    private static Document createEmptyDocument()
        throws DaemonModelUtilException
    {
        try
        {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

            return builderFactory.newDocumentBuilder().newDocument();
        }
        catch ( ParserConfigurationException e )
        {
            throw new DaemonModelUtilException( "Error while creating DOM Document.", e );
        }
    }

    private static void close( Closeable closeable )
    {
        try
            {
                if ( closeable != null )
            {
                closeable.close();
            }
        }
        catch ( IOException e )
        {
            // ignore
        }
    }
}
