package org.codehaus.mojo.appassembler;

import org.apache.maven.artifact.Artifact;

import junit.framework.TestCase;

public class CreateRepositoryTest extends TestCase
{
    public void testResolveBooterArtifact() throws Exception
    {
        CreateRepositoryMojo mojo = new CreateRepositoryMojo();
        Artifact booter = mojo.resolveBooterArtifact(); 
        assertNotNull(  booter );
        booter.isSnapshot();
      //  assertNotNull("Could not find booter artifact", booter.getFile());
    }
}
