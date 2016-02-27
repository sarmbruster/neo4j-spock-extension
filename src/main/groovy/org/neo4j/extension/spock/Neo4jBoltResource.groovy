package org.neo4j.extension.spock

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.harness.internal.Ports
import org.neo4j.test.TestGraphDatabaseFactory

/**
 * a junit external resource implementation for providing a {@link GraphDatabaseService} with a bolt configuration
 * Additionally a cypher method is activated on Strings
 */
class Neo4jBoltResource extends Neo4jResource {

    Map config = [:]
    GraphDatabaseService graphDatabaseService
    String boltUrl

    @Override
    protected void before() throws Throwable {
        def iaddress = Ports.findFreePort("localhost", [7687, 20000] as int[])
        config["dbms.connector.0.enabled"] = "true"
        config["dbms.connector.0.tls.level"] = "OPTIONAL"

        def url = "${iaddress.hostName}:${iaddress.port}"
        boltUrl = "bolt://$url"
        config["dbms.connector.0.address"] = url.toString()

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
