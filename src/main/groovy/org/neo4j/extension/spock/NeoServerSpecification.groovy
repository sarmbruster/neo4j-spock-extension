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

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.server.NeoServer
import org.neo4j.server.helpers.CommunityServerBuilder
import spock.lang.Shared
import spock.lang.Specification

/**
 * abstract base class for spock tests using a Neo4j server
 */
abstract class NeoServerSpecification extends Specification {

    @Shared GraphDatabaseService graphDatabaseService
    @Shared NeoServer server
    @Shared int port = 37474
    @Shared thirdPartyJaxRsPackages = [:]
//    @Shared Client client = Client.create()
    @Shared String baseUrl

    def setupSpec() {
        def serverBuilder = CommunityServerBuilder.server()

        thirdPartyJaxRsPackages.each { packageName, mountPoint ->
            serverBuilder.withThirdPartyJaxRsPackage(packageName, mountPoint)
        }

        server = serverBuilder.onPort(port).build();
        server.start()
        baseUrl = server.baseUri().toASCIIString()
        graphDatabaseService = server.database.graph
    }


    def cleanupSpec() {
        server.stop()
    }

    def withTransaction(Closure closure) {
        def tx = graphDatabaseService.beginTx()
        try {
            def result = closure.call()

            tx.success()
            return result
        } finally {
            tx.close()
        }
    }

}
