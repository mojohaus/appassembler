package org.codehaus.mojo.appassembler;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorService;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @goal generate-daemons
 * @requiresDependencyResolution runtime
 * @phase generate-resources
 */
public class GenerateDaemonsMojo
    extends AbstractMojo
{
    // -----------------------------------------------------------------------
    // Parameters
    // -----------------------------------------------------------------------

    /**
     * @parameter
     * @required
     */
    private Set daemons;

    /**
     * @parameter
     */
    private JvmSettings defaultJvmSettings;

    /**
     * @parameter expression="${basedir}"
     * @required
     */
    private File basedir;

    /**
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File target;

    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    // -----------------------------------------------------------------------
    // Read-only parameters
    // -----------------------------------------------------------------------

    /**
     * @readonly
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;

    // -----------------------------------------------------------------------
    // Components
    // -----------------------------------------------------------------------

    /**
     * @component org.codehaus.mojo.appassembler.daemon.DaemonGeneratorService
     */
    private DaemonGeneratorService daemonGeneratorService;

    // -----------------------------------------------------------------------
    // AbstractMojo Implementation
    // -----------------------------------------------------------------------

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            for ( Iterator it = daemons.iterator(); it.hasNext(); )
            {
                Daemon daemon = (Daemon) it.next();

                // -----------------------------------------------------------------------
                // Load the optional template daemon descriptor
                // -----------------------------------------------------------------------

                File descriptor = null;

                if ( !StringUtils.isEmpty( daemon.getDescriptor() ) )
                {
                    descriptor = new File( basedir, daemon.getDescriptor() );
                }

                // -----------------------------------------------------------------------
                //
                // -----------------------------------------------------------------------

                org.codehaus.mojo.appassembler.model.JvmSettings modelJvmSettings = null;

                if ( defaultJvmSettings != null )
                {
                    modelJvmSettings = convertJvmSettings( defaultJvmSettings );
                }

                // -----------------------------------------------------------------------
                // Create a daemon object from the POM configuration
                // -----------------------------------------------------------------------

                org.codehaus.mojo.appassembler.model.Daemon modelDaemon;

                modelDaemon = new org.codehaus.mojo.appassembler.model.Daemon();

                modelDaemon.setId( daemon.getId() );
                modelDaemon.setMainClass( daemon.getMainClass() );
                modelDaemon.setCommandLineArguments( daemon.getCommandLineArguments() );

                if ( daemon.getJvmSettings() != null )
                {
                    modelDaemon.setJvmSettings( convertJvmSettings( daemon.getJvmSettings() ) );
                }
                else
                {
                    modelDaemon.setJvmSettings( modelJvmSettings );
                }

                // -----------------------------------------------------------------------
                //
                // -----------------------------------------------------------------------

                for ( Iterator it2 = daemon.getPlatforms().iterator(); it2.hasNext(); )
                {
                    String platform = (String) it2.next();

                    File output = new File( new File( target, "generated-resources" ), platform );

                    daemonGeneratorService.generateDaemon( platform, descriptor, modelDaemon, output, project,
                                                           localRepository );
                }
            }
        }
        catch ( DaemonGeneratorException e )
        {
            throw new MojoExecutionException( "Error while generating daemon.", e );
        }
    }

    private org.codehaus.mojo.appassembler.model.JvmSettings convertJvmSettings( JvmSettings jvmSettings )
    {
        org.codehaus.mojo.appassembler.model.JvmSettings modelJvmSettings =
            new org.codehaus.mojo.appassembler.model.JvmSettings();

        modelJvmSettings.setInitialMemorySize( jvmSettings.getInitialMemorySize() );
        modelJvmSettings.setMaxMemorySize( jvmSettings.getMaxMemorySize() );
        modelJvmSettings.setMaxStackSize( jvmSettings.getMaxStackSize() );

        return modelJvmSettings;
    }
}
