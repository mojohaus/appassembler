package org.codehaus.mojo.appassembler;

public class JvmSettings
{
    private String initialMemorySize;

    private String maxMemorySize;

    private String maxStackSize;

    private String[] systemProperties;
    
    private String[] extraArguments;

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

    public String[] getExtraArguments()
    {
        return extraArguments;
    }
}
