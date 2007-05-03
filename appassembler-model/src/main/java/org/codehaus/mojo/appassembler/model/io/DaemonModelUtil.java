package org.codehaus.mojo.appassembler.model.io;

import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.mojo.appassembler.model.Directory;
import org.codehaus.mojo.appassembler.model.JvmSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component
 */
public class DaemonModelUtil
{
    private static SAXParserFactory parserFactory = SAXParserFactory.newInstance();

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

            return load( fileInputStream );
        }
        catch ( SAXException e )
        {
            throw new DaemonModelUtilException( "Error while parsing '" + file + "'", e );
        }
        catch ( FileNotFoundException e )
        {
            throw new DaemonModelUtilException( "Could not find file: '" + file + "'" );
        }
        catch ( ParserConfigurationException e )
        {
            throw new DaemonModelUtilException( "Error while parsing '" + file + "'", e );
        }
        finally
        {
            close( fileInputStream );
        }
    }

    public static Daemon loadModel( InputStream inputStream  )
        throws IOException
    {
        try
        {
            return load( inputStream );
        }
        catch ( SAXException e )
        {
            throw new DaemonModelUtilException( "Error while parsing input stream.", e );
        }
        catch ( ParserConfigurationException e )
        {
            throw new DaemonModelUtilException( "Error while parsing input stream.", e );
        }
        finally
        {
            close( inputStream );
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
        
        private List commandLineArguments;

        private boolean first = true;

        private boolean insideDaemon;

        private boolean insideClasspath;

        private boolean insideJvmSettings;

        private boolean insideDependency;

        private boolean insideDirectory;

        private boolean insideSystemProperties;

        private boolean insideSystemProperty;
        
        private boolean insideCommandLineArguments;
        
        private boolean insideCommandLineArgument;

        private StringBuffer text;

        // -----------------------------------------------------------------------
        // ContentHandler Implementation
        // -----------------------------------------------------------------------

        public void startElement( String uri, String localName, String qName, Attributes attributes )
            throws SAXException
        {
            if ( first && !qName.equals( "daemon" ) )
            {
                throw new SAXException( "Illegal start tag '" + qName + "', expected 'daemon'." );
            }

            first = false;

            if ( qName.equals( "daemon" ) )
            {
                daemon = new Daemon();

                insideDaemon = true;
            }
            else if ( insideDaemon )
            {
                if ( qName.equals( "classpath" ) )
                {
                    insideClasspath = true;

                    classpathElements = new ArrayList();
                }
                else if ( qName.equals( "jvmSettings" ) )
                {
                    insideJvmSettings = true;

                    jvmSettings = new JvmSettings();
                }
                else if ( qName.equals( "commandLineArguments" ) )
                {
                    insideCommandLineArguments = true;
                    commandLineArguments = new ArrayList();
                }
                else if ( insideClasspath )
                {
                    if ( qName.equals( "dependency" ) )
                    {
                        insideDependency = true;

                        dependency = new Dependency();
                    }
                    else if ( qName.equals( "directory" ) )
                    {
                        insideDirectory = true;

                        directory = new Directory();
                    }
                }
                else if ( insideJvmSettings )
                {
                    if ( qName.equals( "systemProperties" ) )
                    {
                        insideSystemProperties = true;

                        systemProperties = new ArrayList();
                    }
                    else if ( insideSystemProperties )
                    {
                        if ( qName.equals( "systemProperty" ) )
                        {
                            insideSystemProperty = true;
                        }
                    }
                }
                else if ( insideCommandLineArguments )
                {
                    if ( qName.equals( "commandLineArgument" ) )
                    {
                        insideCommandLineArgument = true;
                    }
                }
            }
        }

        public void characters( char ch[], int start, int length )
            throws SAXException
        {
            if ( text == null )
            {
                text = new StringBuffer(  );
            }
            text.append(ch, start, length  ); 
        }

        public void endElement( String uri, String localName, String qName )
            throws SAXException
        {
            String text = trimText();
            
            if ( qName.equals( "daemon" ) )
            {
                insideDaemon = false;
            }

            if ( insideDaemon )
            {
                if ( qName.equals( "id" ) )
                {
                    daemon.setId( text );
                }
                else if ( qName.equals( "mainClass" ) )
                {
                    daemon.setMainClass( text );
                }
                else if ( qName.equals( "environmentSetupFileName" ) )
                {
                    daemon.setEnvironmentSetupFileName( text );
                }
                else if ( qName.equals( "classpath" ) )
                {
                    insideClasspath = false;

                    daemon.setClasspath( classpathElements );
                }
                else if ( insideClasspath )
                {
                    if ( qName.equals( "dependency" ) )
                    {
                        insideDependency = false;

                        classpathElements.add( dependency );

                        dependency = null;
                    }
                    else if ( qName.equals( "directory" ) )
                    {
                        insideDirectory = false;

                        classpathElements.add( directory );

                        directory = null;
                    }
                    else if ( insideDependency )
                    {
                        if ( qName.equals( "groupId" ) )
                        {
                            dependency.setGroupId( text );
                        }
                        else if ( qName.equals( "artifactId" ) )
                        {
                            dependency.setArtifactId( text );
                        }
                        else if ( qName.equals( "version" ) )
                        {
                            dependency.setVersion( text );
                        }
                        else if ( qName.equals( "relativePath" ) )
                        {
                            dependency.setRelativePath( text );
                        }
                    }
                    else if ( insideDirectory )
                    {
                        if ( qName.equals( "relativePath" ) )
                        {
                            directory.setRelativePath( text );
                        }
                    }
                }
                else if ( insideJvmSettings )
                {
                    if ( qName.equals( "jvmSettings" ) )
                    {
                        daemon.setJvmSettings( jvmSettings );

                        insideJvmSettings = false;
                    }
                    else if ( insideJvmSettings )
                    {
                        if ( qName.equals( "initialMemorySize" ) )
                        {
                            jvmSettings.setInitialMemorySize( text );
                        }
                        else if ( qName.equals( "maxMemorySize" ) )
                        {
                            jvmSettings.setMaxMemorySize( text );
                        }
                        else if ( qName.equals( "maxStackSize" ) )
                        {
                            jvmSettings.setMaxStackSize( text );
                        }
                        else if ( qName.equals( "systemProperties" ) )
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
                else if (insideCommandLineArguments) 
                {
                    if ( qName.equals( "commandLineArguments" ) )
                    {
                        daemon.setCommandLineArguments( commandLineArguments );

                        insideCommandLineArguments = false;
                    }
                    if ( insideCommandLineArgument )
                    {
                        commandLineArguments.add( text );
                        insideCommandLineArgument = false;
                    }
                }                
            }

            text = null;
        }

        private String trimText()
        {
            if ( text == null )
            {
                return null;
            }
            
            String temp = text.toString();
            temp = temp.replace( '\n', ' ' );
            temp = temp.replace( '\t', ' ' );
            temp = temp.trim();
            text = null;
            return temp;
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
        if ( d.getJvmSettings() != null )
        {
            daemon.appendChild( createJvmSettings( document.createElement( "jvmSettings" ), d.getJvmSettings() ) );
        }
        if ( d.getCommandLineArguments() != null )
        {
            daemon.appendChild( createCommandLineArguments( document.createElement( "commandLineArguments" ), d.getCommandLineArguments() ) );
        }
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
    
    private static Node createCommandLineArguments( Element element, List commandLineArguments )
        throws DaemonModelUtilException
    {
        for ( Iterator it = commandLineArguments.iterator(); it.hasNext(); )
        {
            Object o = it.next();
    
            if ( o instanceof String )
            {
                addSimpleElement( element, "commandLineArgument", o.toString() );
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
        if ( value == null || value.trim().length() == 0 )
        {
            return;
        }

        Document document = parent.getOwnerDocument();
        Element element = document.createElement( elementName );
        element.appendChild( document.createTextNode( value ) );
        parent.appendChild( element );
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

    private static void close( InputStream closeable )
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

    private static void close( OutputStream closeable )
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

    private static Daemon load( InputStream inputStream )
        throws SAXException, IOException, ParserConfigurationException
    {
        SAXParser saxParser = parserFactory.newSAXParser();

        DaemonContentHandler daemonContentHandler = new DaemonContentHandler();

        saxParser.parse( inputStream, daemonContentHandler );

        return daemonContentHandler.getDaemon();
    }
}
