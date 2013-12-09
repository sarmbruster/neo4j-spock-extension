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

import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Transaction
import org.neo4j.test.TestGraphDatabaseFactory
import spock.lang.Specification

/**
 * abstract base class for spock tests using a NeoServer
 */
abstract class NeoSpecification extends Specification {

    GraphDatabaseService graphDatabaseService
    Transaction transaction
    ExecutionEngine executionEngine

    def setup() {
        graphDatabaseService = new TestGraphDatabaseFactory().newImpermanentDatabase()
        transaction = graphDatabaseService.beginTx()
        executionEngine = new ExecutionEngine(graphDatabaseService)
        CypherMixin.executionEngine = executionEngine
        String.mixin CypherMixin
    }

    def cleanup() {
        transaction.close()
        graphDatabaseService.shutdown()
    }

}
