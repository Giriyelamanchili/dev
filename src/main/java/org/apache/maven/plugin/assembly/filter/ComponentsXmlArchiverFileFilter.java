package org.apache.maven.plugin.assembly.filter;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.archiver.AbstractArchiveFinalizer;
import org.codehaus.plexus.archiver.ArchiveFileFilter;
import org.codehaus.plexus.archiver.ArchiveFilterException;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Components XML file filter.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class ComponentsXmlArchiverFileFilter
    extends AbstractArchiveFinalizer
    implements ArchiveFileFilter
{
    // [jdcasey] Switched visibility to protected to allow testing. Also, because this class isn't final, it should allow
    // some minimal access to the components accumulated for extending classes.
    protected Map components;

    public static final String COMPONENTS_XML_PATH = "META-INF/plexus/components.xml";
    
    public boolean include( InputStream dataStream, String entryName )
        throws ArchiveFilterException
    {
        String entry = entryName;
        
        if ( entry.startsWith( "/" ) )
        {
            entry = entry.substring( 1 );
        }
        
        if ( ComponentsXmlArchiverFileFilter.COMPONENTS_XML_PATH.equals( entry ) )
        {
            try
            {
                addComponentsXml( new InputStreamReader( dataStream ) );
            }
            catch ( XmlPullParserException e )
            {
                throw new ArchiveFilterException( "Error reading components from stream: " + e.getMessage(), e );
            }
            catch ( IOException e )
            {
                throw new ArchiveFilterException( "Error reading components from stream: " + e.getMessage(), e );
            }
            
            return false;
        }
        
        return true;
    }
    
    protected void addComponentsXml( Reader componentsReader )
        throws XmlPullParserException, IOException
    {
        Xpp3Dom newDom = Xpp3DomBuilder.build( componentsReader );

        if ( newDom != null )
        {
            newDom = newDom.getChild( "components" );
        }
        
        if ( newDom != null )
        {
            Xpp3Dom[] children = newDom.getChildren();
            for ( int i = 0; i < children.length; i++ )
            {
                Xpp3Dom component = children[i];

                if ( components == null )
                {
                    components = new LinkedHashMap();
                }

                String role = component.getChild( "role" ).getValue();
                Xpp3Dom child = component.getChild( "role-hint" );
                String roleHint = child != null ? child.getValue() : "";
                components.put( role + roleHint, component );
            }
        }
    }
    
//    public void addComponentsXml( File componentsXml )
//        throws IOException, XmlPullParserException
//    {
//        FileReader fileReader = null;
//        try
//        {
//            fileReader = new FileReader( componentsXml );
//            
//            addComponentsXml( fileReader );
//        }
//        finally
//        {
//            IOUtil.close( fileReader );
//        }
//    }

    private void addToArchive( Archiver archiver )
        throws IOException, ArchiverException
    {
        if ( components != null )
        {
            File f = File.createTempFile( "maven-assembly-plugin", "tmp" );
            f.deleteOnExit();

            FileWriter fileWriter = new FileWriter( f );
            try
            {
                Xpp3Dom dom = new Xpp3Dom( "component-set" );
                Xpp3Dom componentDom = new Xpp3Dom( "components" );
                dom.addChild( componentDom );

                for ( Iterator i = components.values().iterator(); i.hasNext(); )
                {
                    Xpp3Dom component = (Xpp3Dom) i.next();
                    componentDom.addChild( component );
                }

                Xpp3DomWriter.write( fileWriter, dom );
            }
            finally
            {
                IOUtil.close( fileWriter );
            }
            archiver.addFile( f, COMPONENTS_XML_PATH );
        }
    }

    public void finalizeArchiveCreation( Archiver archiver )
        throws ArchiverException
    {
        try
        {
            addToArchive( archiver );
        }
        catch ( IOException e )
        {
            throw new ArchiverException( "Error finalizing component-set for archive. Reason: " + e.getMessage(), e );
        }
    }

}
