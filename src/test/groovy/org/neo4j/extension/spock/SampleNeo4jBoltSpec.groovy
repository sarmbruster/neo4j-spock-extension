package org.neo4j.extension.spock

import org.junit.Rule
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Session
import org.neo4j.driver.v1.exceptions.ClientException
import spock.lang.Specification

class SampleNeo4jBoltSpec extends Specification {

    /**
     * provide Neo4j stuff: graphDatabaseService and executionEngine
     */
    @Rule
    @Delegate(interfaces=false)
    Neo4jBoltResource neo4jResource = new Neo4jBoltResource( )

    def "we can access db via bolt"() {
        when:
        Driver driver = GraphDatabase.driver( boltUrl );
        Session session = driver.session();
        session.run("CREATE (n) RETURN n");

        then:
        noExceptionThrown()
    }

    def "connecting to invalid bolt url result in exception"() {
        when:
        Driver driver = GraphDatabase.driver( "bolt://localhost:1234" );
        Session session = driver.session();
        session.run("CREATE (n) RETURN n");

        then:
        def e = thrown(ClientException)
        e.message == "Unable to connect to 'localhost' on port 1234, ensure the database is running and that there is a working network connection to it."

    }

}
