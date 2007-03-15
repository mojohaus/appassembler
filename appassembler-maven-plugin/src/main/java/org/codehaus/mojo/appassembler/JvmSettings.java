package org.codehaus.mojo.appassembler;

public class JvmSettings
{
    private String initialMemorySize;

    private String maxMemorySize;

    private String maxStackSize;

    private String[] systemProperties;

    public String getInitialMemorySize()
    {
        return initialMemorySize;
    }

    public String getMaxMemorySize()
    {
        return maxMemorySize;
    }

    public String getMaxStackSize()
    {
        return maxStackSize;
    }

    public String[] getSystemProperties()
    {
        return systemProperties;
    }

}
