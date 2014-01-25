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
import org.neo4j.helpers.Clock
import org.neo4j.kernel.impl.transaction.xaframework.ForceMode
import org.neo4j.server.CommunityNeoServer
import org.neo4j.server.configuration.Configurator
import org.neo4j.server.configuration.PropertyFileConfigurator
import org.neo4j.server.configuration.ThirdPartyJaxRsPackage
import org.neo4j.server.configuration.validation.DatabaseLocationMustBeSpecifiedRule
import org.neo4j.server.configuration.validation.Validator
import org.neo4j.server.database.CommunityDatabase
import org.neo4j.server.database.Database
import org.neo4j.server.database.EphemeralDatabase
import org.neo4j.server.helpers.CommunityServerBuilder
import org.neo4j.server.preflight.PreFlightTasks
import org.neo4j.server.rest.paging.LeaseManager
import org.neo4j.server.rest.web.DatabaseActions

import static org.neo4j.helpers.Clock.SYSTEM_CLOCK

/**
 * allow to set config properties from neo4j.properties, e.g. execution_guard_enabled
 */
@CompileStatic
class ConfigurableServerBuilder extends CommunityServerBuilder {

    final Map<String,String> config = [:]

    ConfigurableServerBuilder() {
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
    File createPropertiesFiles() throws IOException {
        def file = super.createPropertiesFiles()
        return file
    }

/**
     * mostly a copy of {@link CommunityServerBuilder#build()} but allowing to tweak graphdb's config by hooking into getDbTuningPropertiesWithServerDefaults
     * @return
     * @throws IOException
     */
    @Override
    public CommunityNeoServer build() throws IOException
    {
        if ( dbDir == null && persistent )
        {
            throw new IllegalStateException( "Must specify path" );
        }
        final File configFile = createPropertiesFiles();

        if ( preflightTasks == null )
        {
            preflightTasks = new PreFlightTasks(null) {
                @Override
                public boolean run()
                {
                    return true;
                }
            };
        }

        return new ConfigurableTestCommunityNeoServer( new FixedPropertyFileConfigurator( new Validator(
                new DatabaseLocationMustBeSpecifiedRule() ), configFile ), configFile, config );
    }

    /**
     * subclassed version to fix neo4j:neo4j-1855
     */
    @CompileStatic
    private class FixedPropertyFileConfigurator extends PropertyFileConfigurator {

        FixedPropertyFileConfigurator(Validator v, File propertiesFile) {
            super(v, propertiesFile)
        }

        @Override
        Set<ThirdPartyJaxRsPackage> getThirdpartyJaxRsPackages() {
            Set<ThirdPartyJaxRsPackage> thirdPartyPackages = new LinkedHashSet<>();
            List<String> packagesAndMountpoints = this.configuration().getList( THIRD_PARTY_PACKAGES_KEY );

            for ( String packageAndMoutpoint : packagesAndMountpoints )
            {
                String[] parts = packageAndMoutpoint.split( "=" );
                if ( parts.length != 2 )
                {
                    throw new IllegalArgumentException( "config for " + THIRD_PARTY_PACKAGES_KEY + " is wrong: " +
                            packageAndMoutpoint );
                }
                String pkg = parts[0];
                String mountPoint = parts[1];

                thirdPartyPackages.add( new ThirdPartyJaxRsPackage( pkg, mountPoint ) );
            }
            return thirdPartyPackages;
        }
    }

    /** copied from CommunityServerBuilder
     * difference is that custom config gets injected into tuning params. As a side effect we can basically inject all parameters from neo4j.properties
      */
    @CompileStatic
    private class ConfigurableTestCommunityNeoServer extends CommunityNeoServer {
        private final File configFile;
        private final Map<String, String> graphDbConfig

        ConfigurableTestCommunityNeoServer(PropertyFileConfigurator propertyFileConfigurator, File configFile, Map<String,String> config) {
            super(propertyFileConfigurator);

            this.configFile = configFile;
            this.graphDbConfig = config
        }

        @Override
        protected PreFlightTasks createPreflightTasks() {
            return preflightTasks;
        }

        @Override
        protected Database createDatabase() {
            return persistent ?
                    new CommunityDatabase(configurator) {
                        @Override
                        protected Map<String, String> getDbTuningPropertiesWithServerDefaults() {
                            Map map = super.getDbTuningPropertiesWithServerDefaults()
                            map.putAll(graphDbConfig)
                            map
                        }
                    } :
                    new EphemeralDatabase(configurator) {
                        @Override
                        protected Map<String, String> getDbTuningPropertiesWithServerDefaults() {
                            Map map = super.getDbTuningPropertiesWithServerDefaults()
                            map.putAll(graphDbConfig)
                            map
                        }
                    };
        }

        @Override
        protected DatabaseActions createDatabaseActions() {
            Clock clockToUse = (clock != null) ? clock : SYSTEM_CLOCK;

            return new DatabaseActions(
                    new LeaseManager(clockToUse),
                    ForceMode.forced,
                    configurator.configuration().getBoolean(
                            Configurator.SCRIPT_SANDBOXING_ENABLED_KEY,
                            Configurator.DEFAULT_SCRIPT_SANDBOXING_ENABLED as boolean) as boolean, database.getGraph());
        }

        @Override
        public void stop() {
            super.stop();
            configFile.delete();
        }
    }
}
