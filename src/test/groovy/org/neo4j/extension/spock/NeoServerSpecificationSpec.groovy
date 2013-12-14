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

import org.neo4j.kernel.guard.Guard
import org.neo4j.test.server.HTTP

class NeoServerSpecificationSpec extends NeoServerSpecification {

    def getConfig() {
        [
                execution_guard_enabled: "true"
        ]
    }

    def "server is running"() {
        expect:
        baseUrl == "http://localhost:${port}/"
        HTTP.GET(baseUrl).status() == 200
    }

    def "standard cypher endpoint is working"() {
        setup:
        withTransaction {
            graphDatabaseService.createNode()
        }

        when:
        def json = [
                query: "match (n) return count(n)"
        ]

        def response = HTTP.POST("${baseUrl}db/data/cypher", json)

        then:
        response.status() == 200
        response.content().data.size() == 1
        response.content().data[0][0] == 1

    }

    def "transactional cypher endpoint is working"() {

        setup:
        withTransaction {
            graphDatabaseService.createNode()
        }

        when:
        def json = [
                statements: [
                    [
                            statement: "match (n) return count(n)",
                            parameters: [:]
                    ]
                ]
        ]

        def response = HTTP.POST("${baseUrl}db/data/transaction", json)

        then:
        response.status() == 201
        response.content().commit =~ "http://localhost:\\d+/db/data/transaction/1/commit"
        response.content().results.size() == 1
        response.content().results[0].columns[0] == "count(n)"
        response.content().results[0].data.size() == 1
        response.content().results[0].data.row[0][0] == 2

    }

    def "cyphermixin on String is working"() {
        when:
        def result = "match (n) return count(n) as count".cypher()

        then:
        result[0].count instanceof Number
    }


    def "guard is enabled in this specification"() {
        when:
        server.database.graph.dependencyResolver.resolveDependency(Guard)

        then:
        notThrown IllegalArgumentException
    }

}
