package org.neo4j.extension.spock

import org.junit.ClassRule
import org.neo4j.graphdb.NotInTransactionException
import org.neo4j.helpers.collection.Iterables
import org.neo4j.kernel.impl.proc.Procedures
import org.neo4j.kernel.internal.GraphDatabaseAPI
import spock.lang.Shared
import spock.lang.Specification

class SampleNeo4jServerSpec extends Specification {

    /**
     *
     * we need to define our test resources: a neo4j server
     * two options:
     *
     * <ol>
     *     <li>one server per test class: use @Shared and @ClassRule</li>
     *     <li>one server per test method: use @Rule and optionally @Delegate
     * </ol>
     *
     * N.B. @Delegate and @Shared cannot be combined!
     */
    @Shared
    @ClassRule
    Neo4jServerResource neo4j = new Neo4jServerResource(
            config: [execution_guard_enabled: "true"]
    )

    def cleanup() {
        neo4j.closeCypher()  // since we use a class rule here, make sure every test method cleans up separately
    }

    def "server is running"() {
        expect:
        neo4j.baseUrl =~ "http://localhost:\\d+/"

        and: "root page gives http 200"
        neo4j.http.GET("").status() == 200
    }

    def "standard cypher endpoint is working"() {
        setup:
        def nodeCount = getNodeCount()

        when:
        def response = neo4j.postLegacyCypher([query: "match (n) return count(n)"])

        then:
        response.status() == 200
        response.content().data.size() == 1
        response.content().data[0][0] == nodeCount
    }

    def "transactional cypher endpoint is working"() {
        setup:
        def nodeCount = getNodeCount()

        when:
        def response = neo4j.postTransactionalCypher(["match (n) return count(n)"])

        then:
        response.status() == 200
        response.content().results.size() == 1
        response.content().results[0].columns[0] == "count(n)"
        response.content().results[0].data.size() == 1
        response.content().results[0].data.row[0][0] == nodeCount
    }

    def "cypher method on String is working"() {
        when:
        def result = "match (n) return count(n) as count".cypher()

        then:
        result[0].count instanceof Number
    }

    def "there is no transactional scope by default"() {
        when:
        neo4j.graphDatabaseService.createNode()

        then:
        thrown NotInTransactionException
    }

    @WithNeo4jTransaction
    def "withNeo4jTransaction provides transactional scope"() {
        when:
        neo4j.graphDatabaseService.createNode()

        then:
        notThrown NotInTransactionException
    }

    def "check if Neo4jServerResource exposes procedures"() {
        when:
        def procedures = ((GraphDatabaseAPI)neo4j.graphDatabaseService).dependencyResolver.resolveDependency(Procedures)
        def nonSystemProcedures = procedures.allProcedures.grep { !(it.name().namespace()[0] in ["db" ,"sys"]) }

        then: "some procedures from this project have been loaded"
        !nonSystemProcedures.empty

    }

    private def getNodeCount() {
        neo4j.withTransaction {
            Iterables.count(neo4j.graphDatabaseService.allNodes)
        }
    }
}
