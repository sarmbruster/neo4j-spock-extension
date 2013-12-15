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

import org.junit.Rule
import org.neo4j.graphdb.NotInTransactionException
import org.neo4j.helpers.collection.IteratorUtil
import org.neo4j.tooling.GlobalGraphOperations
import org.neo4j.graphdb.Node
import spock.lang.Specification

class SampleNeo4jSpecification extends Specification {

    /**
     * provide Neo4j stuff: graphDatabaseService and executionEngine
     */
    @Rule
    Neo4jResource neo4jResource = new Neo4jResource( config: [execution_guard_enabled: "true"])

    def "graphDatabaseService is available"() {
        expect:
        neo4jResource.graphDatabaseService != null
    }

    def "executionEngine is available"() {
        expect:
        neo4jResource.executionEngine != null
    }

    def "by default a feature method has no transactional context"() {
        when: "trigger a write operation"
        neo4jResource.graphDatabaseService.createNode()

        then:
        thrown(NotInTransactionException)
    }

    @WithNeo4jTransaction
    def "withNeo4jTransaction provides transactional"() {

        expect: "empty database"
        IteratorUtil.count(GlobalGraphOperations.at(neo4jResource.graphDatabaseService).allNodes) == 0

        when:
        neo4jResource.graphDatabaseService.createNode()

        then:
        notThrown NotInTransactionException
        IteratorUtil.count(GlobalGraphOperations.at(neo4jResource.graphDatabaseService).allNodes) == 1
    }

    def "cypher method applied to String class"() {
        when:
        def executionResult = "create (n) return n".cypher()

        then:
        executionResult[0].n instanceof Node
    }

    def "parameterized cypher works on String class"() {
        setup:
        "create (n:Person {props})".cypher(props: [name: 'Stefan'])

        when:
        def executionResult = "match (n:Person) where n.name={name} return n.name as name".cypher(name:'Stefan')

        then:
        executionResult[0].name == 'Stefan'
    }
}
