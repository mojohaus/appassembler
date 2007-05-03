package org.codehaus.mojo.appassembler;

import junit.framework.TestCase;

import org.apache.maven.artifact.Artifact;

public class CreateRepositoryTest
    extends TestCase
{
    public void testResolveBooterArtifact()
        throws Exception
    {
        CreateRepositoryMojo mojo = new CreateRepositoryMojo();
        Artifact booter = mojo.resolveBooterArtifact();
        assertNotNull( booter );
        booter.isSnapshot();
        //  assertNotNull("Could not find booter artifact", booter.getFile());
    }
}
