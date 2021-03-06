package org.apache.maven.plugin.assembly.archive;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.assembly.testutils.MockManager;
import org.apache.maven.plugin.assembly.testutils.TestFileManager;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.util.IOUtil;
import org.easymock.MockControl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;

import junit.framework.TestCase;

public class ManifestCreationFinalizerTest
    extends TestCase
{

    private TestFileManager fileManager = new TestFileManager( "manifest-finalizer.test.", ".jar" );
    
    public void tearDown() throws IOException
    {
        fileManager.cleanUp();
    }

    public void testShouldDoNothingWhenArchiveConfigIsNull()
        throws ArchiverException
    {
        new ManifestCreationFinalizer( null, null ).finalizeArchiveCreation( null );
    }

    public void testShouldDoNothingWhenArchiverIsNotJarArchiver()
        throws ArchiverException
    {
        MockManager mm = new MockManager();

        MockAndControlForArchiver macArchiver = new MockAndControlForArchiver( mm );

        MavenProject project = new MavenProject( new Model() );
        MavenArchiveConfiguration config = new MavenArchiveConfiguration();

        mm.replayAll();

        new ManifestCreationFinalizer( project, config ).finalizeArchiveCreation( macArchiver.archiver );

        mm.verifyAll();
    }

    public void testShouldAddManifestWhenArchiverIsJarArchiver()
        throws ArchiverException, IOException
    {
        MavenProject project = new MavenProject( new Model() );
        MavenArchiveConfiguration config = new MavenArchiveConfiguration();
        
        File tempDir = fileManager.createTempDir();
        
        File manifestFile = fileManager.createFile( tempDir, "MANIFEST.MF", "Main-Class: Stuff" );
        
        config.setManifestFile( manifestFile );
        
        Archiver archiver = new JarArchiver();

        File file = fileManager.createTempFile();
        
        archiver.setDestFile( file );
        
        new ManifestCreationFinalizer( project, config ).finalizeArchiveCreation( archiver );
        
        archiver.createArchive();
        
        URL resource = new URL( "jar:file:" + file.getAbsolutePath() + "!/META-INF/MANIFEST.MF" );
        
        BufferedReader reader = new BufferedReader( new InputStreamReader( resource.openStream() ) );
        
        StringWriter writer = new StringWriter();
        
        IOUtil.copy( reader, writer );
        
        assertTrue( writer.toString().indexOf( "Main-Class: Stuff" ) > -1 );
    }

    private final class MockAndControlForArchiver
    {
        Archiver archiver;

        MockControl control;

        MockAndControlForArchiver( MockManager mm )
        {
            control = MockControl.createControl( Archiver.class );
            mm.add( control );

            archiver = ( Archiver ) control.getMock();
        }
    }

}
