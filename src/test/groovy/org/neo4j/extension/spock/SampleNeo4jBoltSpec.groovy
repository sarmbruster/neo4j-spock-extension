package org.neo4j.extension.spock

import org.junit.Rule
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Session
import org.neo4j.driver.v1.exceptions.ConnectionFailureException
import spock.lang.Specification

class SampleNeo4jBoltSpec extends Specification {

    /**
     * provide Neo4j stuff: graphDatabaseService and executionEngine
     */
    @Rule
    @Delegate(interfaces=false)
    Neo4jBoltResource neo4jResource = new Neo4jBoltResource( )

    def "we can access db via bolt"() {
        when: "using provided session from Neo4jBoltResource"
        def result = session.run("CREATE (n) RETURN count(n) as c");

        then:
        noExceptionThrown()

        and:
        result.single().get("c").asInt() == 1
        //result.single().c.asInt() == 1
    }

    def "connecting to invalid bolt url result in exception"() {
        when:
        Driver driver = GraphDatabase.driver( "bolt://localhost:1234" );
        Session session = driver.session();
        session.run("CREATE (n) RETURN n");

        then:
        def e = thrown(ConnectionFailureException)
        e.message == "Unable to connect to localhost:1234, ensure the database is running and that there is a working network connection to it."

    }

}
