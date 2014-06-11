/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.neo4j.extension.spock

import groovy.transform.CompileStatic
import org.neo4j.kernel.GraphDatabaseAPI
import org.neo4j.kernel.InternalAbstractGraphDatabase
import org.neo4j.kernel.logging.Logging
import org.neo4j.server.CommunityNeoServer
import org.neo4j.server.configuration.Configurator
import org.neo4j.server.database.Database
import org.neo4j.server.database.LifecycleManagingDatabase
import org.neo4j.server.helpers.CommunityServerBuilder
import org.neo4j.server.preflight.PreFlightTasks
import org.neo4j.test.ImpermanentGraphDatabase

import static org.neo4j.server.database.LifecycleManagingDatabase.lifecycleManagingDatabase

/**
 * allow to set config properties from neo4j.properties, e.g. execution_guard_enabled
 */
@CompileStatic
class ConfigurableServerBuilder extends CommunityServerBuilder {

    final Map<String, String> config = [:]

    ConfigurableServerBuilder() {
        super(null)
        // workaround for neo4j:neo4j-1855: use LinkedHashMap for thirdPartyJaxRsPackage in super class
        def f = CommunityServerBuilder.class.getDeclaredField("thirdPartyPackages")
        f.setAccessible(true)
        f.set(this, new LinkedHashMap<>());
    }

    ConfigurableServerBuilder withConfigProperty(String key, String value) {
        config[key] = value
        this
    }

    @Override
    protected CommunityNeoServer build(File configFile, Configurator configurator, Logging logging) {
        Database.Factory databaseFactory = createDatabaseFactory()
        new ConfigurableCommunityNeoServer(configurator, databaseFactory, logging)
    }

    protected Database.Factory createDatabaseFactory() {
        return lifecycleManagingDatabase(new LifecycleManagingDatabase.GraphFactory() {
            @Override
            GraphDatabaseAPI newGraphDatabase(String storeDir, Map<String, String> params, InternalAbstractGraphDatabase.Dependencies dependencies) {
                params.put( InternalAbstractGraphDatabase.Configuration.ephemeral.name(), "true" );
                config.each { String k, String v ->
                    params.put( k, v)

                }
                return new ImpermanentGraphDatabase( storeDir, params, dependencies );
            }
        })
    }


    class ConfigurableCommunityNeoServer extends CommunityNeoServer {
        final File configFile;

        ConfigurableCommunityNeoServer(Configurator propertyFileConfigurator, Database.Factory databaseFactory, Logging logging) {
            super(propertyFileConfigurator, databaseFactory, logging);
            this.configFile = configFile;
        }

        @Override
        protected PreFlightTasks createPreflightTasks() {
            new PreFlightTasks( logging );  // omitting all standard preflight tasks here intentionally
        }

    }

}
