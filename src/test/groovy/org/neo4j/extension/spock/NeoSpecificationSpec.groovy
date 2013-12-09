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

import org.neo4j.graphdb.NotInTransactionException
import org.neo4j.helpers.collection.IteratorUtil
import org.neo4j.tooling.GlobalGraphOperations

class NeoSpecificationSpec extends NeoSpecification {

    def "graphDatabaseService is available"() {
        expect:
        graphDatabaseService != null
    }

    def "transactional context is available"() {

        expect: "only reference node is there"
        IteratorUtil.count(GlobalGraphOperations.at(graphDatabaseService).allNodes) == 0

        when:
        graphDatabaseService.createNode()

        then:
        notThrown NotInTransactionException
        IteratorUtil.count(GlobalGraphOperations.at(graphDatabaseService).allNodes) == 1
    }

    def "cypher mixin applied to String class"() {
        when:
        def executionResult = "match n return count(n) as c".cypher()

        then:
        executionResult[0].c == 0
    }

    def "parameterized cypher works on String class"() {
        setup:
        "create (n:Person {name:'Stefan'})".cypher()

        when:
        def executionResult = "match (n:Person) where n.name={name} return n.name as name".cypher(name:'Stefan')

        then:
        executionResult[0].name == 'Stefan'
    }

}
