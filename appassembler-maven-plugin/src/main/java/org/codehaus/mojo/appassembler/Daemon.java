package org.codehaus.mojo.appassembler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Daemon
{
    private String id;

    private String mainClass;

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

    private List commandLineArguments;

    private JvmSettings jvmSettings;

//    private Properties properties;

    public String getId()
    {
        return id;
    }

    public String getMainClass()
    {
        return mainClass;
    }

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

    public List getCommandLineArguments()
    {
        return commandLineArguments;
    }

    public JvmSettings getJvmSettings()
    {
        return jvmSettings;
    }
}
