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

import org.neo4j.test.server.HTTP

class NeoServerSpecificationSpec extends NeoServerSpecification {

    def "server is running"() {
        expect:
        baseUrl == "http://localhost:${port}/"
        HTTP.GET(baseUrl).status() == 200
    }

    def "cypher endpoint is working"() {
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

    def "cyphermixin on String is working"() {
        when:
        def result = "match (n) return count(n) as count".cypher()

        then:
        result[0].count == 1
    }
}
