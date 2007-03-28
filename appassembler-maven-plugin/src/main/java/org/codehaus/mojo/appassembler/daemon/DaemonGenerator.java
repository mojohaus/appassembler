package org.codehaus.mojo.appassembler.daemon;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface DaemonGenerator
{
    String ROLE = DaemonGenerator.class.getName();

    void generate( DaemonGenerationRequest generationRequest )
        throws DaemonGeneratorException;
}
