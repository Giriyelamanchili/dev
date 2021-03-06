package org.apache.maven.plugin.assembly.utils;

import org.apache.maven.plugin.assembly.archive.ArchiveExpansionException;
import org.apache.maven.plugin.assembly.format.AssemblyFormattingException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.util.IOUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

public final class AssemblyFileUtils
{

    public static final String LINE_ENDING_KEEP = "keep";
    public static final String LINE_ENDING_DOS = "dos";
    public static final String LINE_ENDING_UNIX = "unix";
    public static final String LINE_ENDING_CRLF = "crlf";
    public static final String LINE_ENDING_LF = "lf";

    private AssemblyFileUtils()
    {
    }

    /**
     * Unpacks the archive file.
     * 
     * @param source
     *            File to be unpacked.
     * @param destDir
     *            Location where to put the unpacked files.
     */
    public static void unpack( File source, File destDir, ArchiverManager archiverManager )
        throws ArchiveExpansionException, NoSuchArchiverException
    {
        try
        {
            UnArchiver unArchiver = archiverManager.getUnArchiver( source );

            unArchiver.setSourceFile( source );

            unArchiver.setDestDirectory( destDir );

            unArchiver.extract();
        }
        catch ( IOException e )
        {
            throw new ArchiveExpansionException( "Error unpacking file: " + source + "to: " + destDir, e );
        }
        catch ( ArchiverException e )
        {
            throw new ArchiveExpansionException( "Error unpacking file: " + source + "to: " + destDir, e );
        }
    }
    
    /**
     * NOTE: It is the responsibility of the caller to close the source Reader instance.
     * @param lineEndings This is the result of the getLineEndingChars(..) method in this utility class; the actual
     *   line-ending characters.
     */
    public static void convertLineEndings( Reader source, File dest, String lineEndings )
        throws IOException
    {
        BufferedWriter out = null;
        BufferedReader bufferedSource = null;
        try
        {
            if ( source instanceof BufferedReader )
            {
                bufferedSource = (BufferedReader) source;
            }
            else
            {
                bufferedSource = new BufferedReader( source );
            }
            
            out = new BufferedWriter( new FileWriter( dest ) );

            String line;

            do
            {
                line = bufferedSource.readLine();
                if ( line != null )
                {
                    out.write( line );
                    out.write( lineEndings );
                }
            } while ( line != null );

            out.flush();
        }
        finally
        {
            IOUtil.close( out );
        }
    }

    public static String getLineEndingCharacters( String lineEnding )
        throws AssemblyFormattingException
    {
        String value = lineEnding;
        if ( lineEnding != null )
        {
            if ( LINE_ENDING_KEEP.equals( lineEnding ) )
            {
                value = null;
            }
            else if ( LINE_ENDING_DOS.equals( lineEnding ) || LINE_ENDING_CRLF.equals( lineEnding ) )
            {
                value = "\r\n";
            }
            else if ( LINE_ENDING_UNIX.equals( lineEnding ) || LINE_ENDING_LF.equals( lineEnding ) )
            {
                value = "\n";
            }
            else
            {
                throw new AssemblyFormattingException( "Illlegal lineEnding specified: '" + lineEnding + "'" );
            }
        }

        return value;
    }
}
