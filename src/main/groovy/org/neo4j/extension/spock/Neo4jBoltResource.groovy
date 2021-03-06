package org.neo4j.extension.spock

import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Session
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.harness.internal.Ports

/**
 * a junit external resource implementation for providing a {@link GraphDatabaseService} with a bolt configuration
 * Additionally a cypher method is activated on Strings
 */
class Neo4jBoltResource extends Neo4jResource {

    String boltUrl
    Driver driver
    Session session

    @Override
    protected void before() throws Throwable {
        super.before()
        driver = GraphDatabase.driver(boltUrl)
        session = driver.session()
    }

    @Override
    protected void doConfigure() {
        def iaddress = Ports.findFreePort("localhost", [7687, 20000] as int[])
        def url = "${iaddress.hostName}:${iaddress.port}"
        boltUrl = "bolt://$url"
        config["dbms.connector.bolt.enabled"]="true"
        config["dbms.connector.bolt.listen_address"]=url.toString()
    }

    @Override
    protected void after() {
        session.close()
        driver.close()
        super.after()
    }
}
