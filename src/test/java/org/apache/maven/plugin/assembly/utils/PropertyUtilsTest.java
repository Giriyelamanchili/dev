package org.apache.maven.plugin.assembly.utils;

import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

public class PropertyUtilsTest
    extends TestCase
{

    public void testShouldNotTouchPropertiesWithNoExpressions()
        throws IOException
    {
        Properties props = new Properties();
        props.setProperty( "key", "value" );
        props.setProperty( "key2", "value2" );

        File propsFile = getTempFile();

        writePropertiesTo( propsFile, props );

        Properties result = PropertyUtils.getInterpolatedPropertiesFromFile( propsFile, true, false );

        assertEquals( "value", result.getProperty( "key" ) );
        assertEquals( "value2", result.getProperty( "key2" ) );
    }

    public void testShouldResolveExpressionReferringToExistingKeyWithoutExpressionPrefix()
        throws IOException
    {
        Properties props = new Properties();
        props.setProperty( "key", "value" );
        props.setProperty( "key2", "${key}" );

        File propsFile = getTempFile();

        writePropertiesTo( propsFile, props );

        Properties result = PropertyUtils.getInterpolatedPropertiesFromFile( propsFile, true, false );

        assertEquals( "value", result.getProperty( "key" ) );
        assertEquals( "value", result.getProperty( "key2" ) );
    }

    public void testShouldResolveExpressionReferringToExistingKeyWithExpressionPrefix()
        throws IOException
    {
        Properties props = new Properties();
        props.setProperty( "key", "value" );
        props.setProperty( "key2", "${__properties.key}" );

        File propsFile = getTempFile();

        writePropertiesTo( propsFile, props );

        Properties result = PropertyUtils.getInterpolatedPropertiesFromFile( propsFile, true, false );

        assertEquals( "value", result.getProperty( "key" ) );
        assertEquals( "value", result.getProperty( "key2" ) );
    }

    public void testShouldResolveExpressionReferringToSysPropKeyWithoutExpressionPrefix()
        throws IOException
    {
        Properties props = new Properties();
        props.setProperty( "key", "value" );
        props.setProperty( "key2", "${user.name}" );
        
        String userName = System.getProperty( "user.name" );

        File propsFile = getTempFile();

        writePropertiesTo( propsFile, props );

        Properties result = PropertyUtils.getInterpolatedPropertiesFromFile( propsFile, true, true );

        assertEquals( userName, result.getProperty( "key2" ) );
    }

    public void testShouldResolveExpressionReferringToSysPropKeyWithExpressionPrefix()
        throws IOException
    {
        Properties props = new Properties();
        props.setProperty( "key", "value" );
        props.setProperty( "key2", "${__properties.user.name}" );
        
        String userName = System.getProperty( "user.name" );

        File propsFile = getTempFile();

        writePropertiesTo( propsFile, props );

        Properties result = PropertyUtils.getInterpolatedPropertiesFromFile( propsFile, true, true );

        assertEquals( userName, result.getProperty( "key2" ) );
    }

    public void testShouldNotTouchExpressionReferringToNonExistentKeyWithoutExpressionPrefix()
        throws IOException
    {
        Properties props = new Properties();
        props.setProperty( "key", "value" );
        props.setProperty( "key2", "${foo.bar.gobbledy.gook}" );

        File propsFile = getTempFile();

        writePropertiesTo( propsFile, props );

        Properties result = PropertyUtils.getInterpolatedPropertiesFromFile( propsFile, true, false );

        assertEquals( "${foo.bar.gobbledy.gook}", result.getProperty( "key2" ) );
    }

    public void testShouldNotTouchExpressionReferringToNonExistentKeyWithExpressionPrefix()
        throws IOException
    {
        Properties props = new Properties();
        props.setProperty( "key", "value" );
        props.setProperty( "key2", "${__properties.foo.bar.gobbledy.gook}" );

        File propsFile = getTempFile();

        writePropertiesTo( propsFile, props );

        Properties result = PropertyUtils.getInterpolatedPropertiesFromFile( propsFile, true, false );

        assertEquals( "${__properties.foo.bar.gobbledy.gook}", result.getProperty( "key2" ) );
    }

    public void testShouldNotIncludeSystemProperties()
        throws IOException
    {
        Properties props = new Properties();

        File propsFile = getTempFile();

        writePropertiesTo( propsFile, props );

        Properties result = PropertyUtils.getInterpolatedPropertiesFromFile( propsFile, true, false );

        assertNull( result.getProperty( "user.name" ) );
    }

    public void testShouldFailWhenGivenNonExistentPropertiesFileLocation()
        throws IOException
    {
        File propsFile = getTempFile();
        propsFile.delete();

        try
        {
            PropertyUtils.getInterpolatedPropertiesFromFile( propsFile, true, false );

            fail( "Should throw FileNotFoundException when properties file doesn't exist, and fail flag is set." );
        }
        catch ( FileNotFoundException e )
        {
            // expected.
        }
    }

    private File getTempFile()
        throws IOException
    {
        File tempFile = File.createTempFile( "properties-test.", "" );
        tempFile.deleteOnExit();

        return tempFile;
    }

    private void writePropertiesTo( File propsFile, Properties properties )
        throws IOException
    {
        FileOutputStream out = null;

        try
        {
            propsFile.getParentFile().mkdirs();

            out = new FileOutputStream( propsFile );
            properties.store( out, "unit test properties file for: " + getMethodAndClass() );
        }
        finally
        {
            IOUtil.close( out );
        }
    }

    private String getMethodAndClass()
    {
        NullPointerException npe = new NullPointerException();
        StackTraceElement callerInfo = npe.getStackTrace()[2];

        return callerInfo.getClassName() + ":" + callerInfo.getMethodName() + "@" + callerInfo.getLineNumber();
    }

}
