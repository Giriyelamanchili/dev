package org.apache.maven.plugin.assembly.repository;

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

import org.apache.maven.plugin.assembly.AssemblerConfigurationSource;
import org.apache.maven.plugins.assembly.model.Repository;
import java.io.File;

/**
 * @author Jason van Zyl
 */
public interface RepositoryAssembler
{
    String ROLE = RepositoryAssembler.class.getName();

    public void assemble( File repositoryDirectory, Repository repository, AssemblerConfigurationSource configSource )
        throws RepositoryAssemblyException;
}
