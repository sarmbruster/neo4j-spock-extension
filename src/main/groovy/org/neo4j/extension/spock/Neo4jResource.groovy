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

import org.junit.rules.ExternalResource
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.test.TestGraphDatabaseFactory

/**
 * a junit external resource implementation for providing a {@link GraphDatabaseService} and a {@link ExecutionEngine}.
 * Additionally a cypher method is activated on Strings
 */
class Neo4jResource extends ExternalResource {

    Map config = [:]
    GraphDatabaseService graphDatabaseService

    @Lazy
    ExecutionEngine executionEngine = new ExecutionEngine(graphDatabaseService)

    @Override
    protected void before() throws Throwable {
        def builder = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder()
        graphDatabaseService =  builder.setConfig(config).newGraphDatabase()
        String.metaClass.cypher = { -> executionEngine.execute(delegate)}
        String.metaClass.cypher = { Map params -> executionEngine.execute(delegate, params)}
    }

    @Override
    protected void after() {
        graphDatabaseService.shutdown()
    }
}
