package org.neo4j.extension.spock

import org.junit.rules.ExternalResource
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.logging.LogProvider
import org.neo4j.test.TestGraphDatabaseFactory

/**
 * a junit external resource implementation for providing a {@link GraphDatabaseService}
 * Additionally a cypher method is activated on Strings
 */
class Neo4jResource extends ExternalResource implements CypherOnStringTrait {

    Map config = [:]
    GraphDatabaseService graphDatabaseService
    boolean shouldAutoRegisterProcedures = true
    LogProvider userLogProvider = null
    LogProvider internalLogProvider = null

    @Override
    protected void before() throws Throwable {
        doConfigure()
        def testGraphDatabaseFactory = new TestGraphDatabaseFactory()
        if (userLogProvider) {
            testGraphDatabaseFactory = testGraphDatabaseFactory.setUserLogProvider(userLogProvider)
        }
        if (internalLogProvider) {
            testGraphDatabaseFactory = testGraphDatabaseFactory.setInternalLogProvider(internalLogProvider)
        }
        def builder = testGraphDatabaseFactory.newImpermanentDatabaseBuilder()

        graphDatabaseService =  builder.setConfig(config).newGraphDatabase()

        if (shouldAutoRegisterProcedures) {
            Neo4jUtils.registerLocalClassesWithProcedureOrUserFunctionAnnotation(graphDatabaseService)
        }
        initCypherOnString()
    }

    /**
     * hook for subclasses to modify {@see #config} map
     */
    protected void doConfigure() {
        // intentionally empty - to be overriden in subclasses
    }

    @Override
    protected void after() {
        closeCypher()
        Neo4jUtils.assertNoOpenTransaction(graphDatabaseService)
        graphDatabaseService.shutdown()
    }



}
