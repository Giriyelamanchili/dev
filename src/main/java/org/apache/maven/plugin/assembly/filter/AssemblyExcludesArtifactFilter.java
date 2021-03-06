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

import org.apache.maven.artifact.Artifact;

import java.util.List;

/**
 * TODO: include in maven-artifact in future
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class AssemblyExcludesArtifactFilter
    extends AssemblyIncludesArtifactFilter
{
    public AssemblyExcludesArtifactFilter( List patterns )
    {
        super( patterns );
    }

    public AssemblyExcludesArtifactFilter( List patterns, boolean actTransitively )
    {
        super( patterns, actTransitively );
    }

    public boolean include( Artifact artifact )
    {
        boolean shouldInclude = !patternMatches( artifact );
        
        if ( !shouldInclude )
        {
            addFilteredArtifactId( artifact.getId() );
        }
        
        return shouldInclude;
    }

    protected String getFilterDescription()
    {
        return "artifact exclusion filter";
    }
    
    public String toString()
    {
        return "Excludes filter:" + getPatternsAsString();
    }
    
}
