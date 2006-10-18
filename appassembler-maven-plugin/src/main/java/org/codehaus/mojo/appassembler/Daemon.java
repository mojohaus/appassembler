package org.codehaus.mojo.appassembler;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Daemon
{
    private String descriptor;

    private List platforms;

    // TODO:
    /**
     * auto, manual
     */
    private String startType;

    // TODO:
    private String description;

    // TODO:
    private String longDescription;

    // TODO:
    private List commandLineArguments;

    private Properties properties;

    public String getDescriptor()
    {
        return descriptor;
    }

    public List getPlatforms()
    {
        if ( platforms == null )
        {
            platforms = new ArrayList();
        }

        return platforms;
    }

    public Properties getProperties()
    {
        if ( properties == null )
        {
            properties = new Properties();
        }

        return properties;
    }
}
