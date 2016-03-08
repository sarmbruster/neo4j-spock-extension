package org.neo4j.extension.spock

import org.junit.rules.ExternalResource
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.test.TestGraphDatabaseFactory

/**
 * a junit external resource implementation for providing a {@link GraphDatabaseService}
 * Additionally a cypher method is activated on Strings
 */
class Neo4jResource extends ExternalResource implements CypherOnStringTrait {

    Map config = [:]
    GraphDatabaseService graphDatabaseService

    @Override
    protected void before() throws Throwable {
        def builder = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder()
        graphDatabaseService =  builder.setConfig(config).newGraphDatabase()
        initCypherOnString()
    }

    @Override
    protected void after() {
        closeCypher()
        Neo4jUtils.assertNoOpenTransaction(graphDatabaseService)
        graphDatabaseService.shutdown()
    }

}
