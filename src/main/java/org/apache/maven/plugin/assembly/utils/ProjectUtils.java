package org.apache.maven.plugin.assembly.utils;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class ProjectUtils
{

    private ProjectUtils()
    {
    }

    public static Set getProjectModules( MavenProject project, List reactorProjects, Logger logger )
        throws IOException
    {
        Set moduleCandidates = new HashSet( reactorProjects );

        Set modules = new HashSet();

        // we temporarily add the master project to the modules set, since this
        // set is pulling double duty as a set of
        // potential module parents in the tree rooted at the master
        // project...this allows us to use the same looping
        // algorithm below to discover both direct modules of the master project
        // AND modules of those direct modules.
        modules.add( project );

        int changed = 0;

        do
        {
            changed = 0;

            for ( Iterator candidateIterator = moduleCandidates.iterator(); candidateIterator.hasNext(); )
            {
                MavenProject moduleCandidate = (MavenProject) candidateIterator.next();

                if ( moduleCandidate.getFile() == null )
                {
                    logger.warn( "Cannot compute whether " + moduleCandidate.getId() + " is a module of: "
                        + project.getId() + "; it does not have an associated POM file on the local filesystem." );
                    continue;
                }

                for ( Iterator parentIterator = modules.iterator(); parentIterator.hasNext(); )
                {
                    MavenProject potentialParent = (MavenProject) parentIterator.next();

                    if ( potentialParent.getFile() == null )
                    {
                        logger.warn( "Cannot use: " + moduleCandidate.getId()
                            + " as a potential module-parent while computing the module set for: " + project.getId()
                            + "; it does not have an associated POM file on the local filesystem." );
                        continue;
                    }

                    // if this parent has an entry for the module candidate in
                    // the path adjustments map, it's a direct
                    // module of that parent.
                    if ( projectContainsModule( potentialParent, moduleCandidate ) )
                    {
                        // add the candidate to the list of modules (and
                        // potential parents)
                        modules.add( moduleCandidate );

                        // remove the candidate from the candidate pool, because
                        // it's been verified.
                        candidateIterator.remove();

                        // increment the change counter, to show that we
                        // verified a new module on this pass.
                        changed++;
                    }
                }
            }
        } while ( changed != 0 );

        // remove the master project from the modules set, now that we're done
        // using it as a set of potential module
        // parents...
        modules.remove( project );

        return modules;
    }

    private static boolean projectContainsModule( MavenProject mainProject, MavenProject moduleProject )
        throws IOException
    {
        List modules = mainProject.getModules();
        File basedir = mainProject.getBasedir();

        File moduleFile = moduleProject.getFile().getCanonicalFile();
        
        File moduleBasedir = moduleProject.getBasedir();
        
        if ( moduleBasedir == null )
        {
            moduleBasedir = new File( "." );
        }
        
        moduleBasedir = moduleBasedir.getCanonicalFile();

        for ( Iterator it = modules.iterator(); it.hasNext(); )
        {
            String moduleSubpath = (String) it.next();

            File moduleDir = new File( basedir, moduleSubpath ).getCanonicalFile();

            if ( moduleDir.equals( moduleFile ) || moduleDir.equals( moduleBasedir ) )
            {
                return true;
            }
        }

        return false;
    }

}
