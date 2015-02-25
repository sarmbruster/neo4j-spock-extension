package org.neo4j.extension.spock

import org.junit.rules.ExternalResource
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.test.TestGraphDatabaseFactory

/**
 * a junit external resource implementation for providing a {@link GraphDatabaseService} and a {@link ExecutionEngine}.
 * Additionally a cypher method is activated on Strings
 */
class Neo4jResource extends ExternalResource implements GraphDatabaseServiceProvider {

    Map config = [:]
    GraphDatabaseService graphDatabaseService

    @Override
    protected void before() throws Throwable {
        def builder = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder()
        graphDatabaseService =  builder.setConfig(config).newGraphDatabase()
        String.metaClass.cypher = { -> graphDatabaseService.execute(delegate)}
        String.metaClass.cypher = { Map params -> graphDatabaseService.execute(delegate, params)}
    }

    @Override
    protected void after() {
        graphDatabaseService.shutdown()
    }
}
