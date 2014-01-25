/*
 * Copyright 2014 the original author or authors.
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

package org.neo4j.extension.spock.thirdpartyjaxrs.extension1

import org.apache.commons.configuration.Configuration
import org.neo4j.extension.spock.thirdpartyjaxrs.LifeCycles
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.server.NeoServer
import org.neo4j.server.plugins.Injectable
import org.neo4j.server.plugins.SPIPluginLifecycle

class LifeCycle1 implements SPIPluginLifecycle {
    @Override
    Collection<Injectable<?>> start(NeoServer neoServer) {
        LifeCycles.lifeCyclesExecutions << this.class
        return Collections.emptyList()
    }

    @Override
    Collection<Injectable<?>> start(GraphDatabaseService graphDatabaseService, Configuration config) {
        return null
    }

    @Override
    void stop() {

    }
}
