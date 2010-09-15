package org.codehaus.mojo.appassembler;

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

    private List commandLineArguments;

    private JvmSettings jvmSettings;

    private List generatorConfigurations;

    private boolean showConsoleWindow = true;

    private String environmentSetupFileName;

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

    public List getGeneratorConfigurations()
    {
        return generatorConfigurations;
    }

    public boolean isShowConsoleWindow()
    {
        return showConsoleWindow;
    }

    public String getEnvironmentSetupFileName()
    {
        return environmentSetupFileName;
    }

    public void setEnvironmentSetupFileName( String environmentSetupFileName )
    {
        this.environmentSetupFileName = environmentSetupFileName;
    }
    
}
