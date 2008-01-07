package org.codehaus.mojo.appassembler.daemon.standard;

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

import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.daemon.script.AbstactScriptDaemonGenerator;
import org.codehaus.mojo.appassembler.daemon.script.Platform;

/**
 * Generates unix and/or windows wrapperscripts.
 *
 * @plexus.component role-hint="windows"
 */
public class WindowsScriptDaemonGenerator
    extends AbstactScriptDaemonGenerator
{
    public WindowsScriptDaemonGenerator()
    {
        super( Platform.WINDOWS_NAME );
    }

    public void generate( DaemonGenerationRequest generationRequest )
        throws DaemonGeneratorException
    {
        scriptGenerator.createBinScript( getPlatformName(), generationRequest.getDaemon(),
                                         generationRequest.getOutputDirectory() );
    }
}