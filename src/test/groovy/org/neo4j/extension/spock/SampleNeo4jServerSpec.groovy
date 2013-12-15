package org.neo4j.extension.spock

import org.junit.ClassRule
import org.neo4j.helpers.collection.Iterables
import org.neo4j.kernel.guard.Guard
import org.neo4j.test.server.HTTP
import org.neo4j.tooling.GlobalGraphOperations
import spock.lang.Shared
import spock.lang.Specification

class SampleNeo4jServerSpec extends Specification {

    /**
     * we need to define our test resources: a neo4j server
     * two options:
     * <ol>
     *     <li>one server per test class: use @Shared and @ClassRule</li>
     *     <li>one server per test method: use @Rule and optionally @Delegate
     * </ol>
     * N.B. @Delegate and @Shared cannot be combined!
     */
    @Shared
    @ClassRule
    Neo4jServerResource neo4j = new Neo4jServerResource(
            config: [execution_guard_enabled: "true"]
    )

    def "server is running"() {
        expect:
        neo4j.baseUrl == "http://localhost:${neo4j.port}/"

        and: "root page gives http 200"
        neo4j.http.GET("").status() == 200
    }

    def "standard cypher endpoint is working"() {
        setup:
        def nodeCount = getNodeCount()

        when:
        def json = [
                query: "match (n) return count(n)"
        ]

        def response = neo4j.http.POST("db/data/cypher", json)

        then:
        response.status() == 200
        response.content().data.size() == 1
        response.content().data[0][0] == nodeCount

    }

    def "transactional cypher endpoint is working"() {
        setup:
        def nodeCount = getNodeCount()

        when:
        def json = [
                statements: [
                    [
                            statement: "match (n) return count(n)",
                            parameters: [:]
                    ]
                ]
        ]
        def response = neo4j.http.POST("db/data/transaction", json)

        then:
        response.status() == 201
        response.content().commit =~ "http://localhost:\\d+/db/data/transaction/1/commit"
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


    def "guard is enabled in this specification"() {
        when:
        neo4j.server.database.graph.dependencyResolver.resolveDependency(Guard)

        then:
        notThrown IllegalArgumentException
    }

    private def getNodeCount() {
        neo4j.withTransaction {
            Iterables.count(GlobalGraphOperations.at(neo4j.graphDatabaseService).allNodes)
        }
    }

}
