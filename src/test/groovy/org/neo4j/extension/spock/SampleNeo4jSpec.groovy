package org.neo4j.extension.spock

import org.junit.Rule
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.NotInTransactionException
import org.neo4j.helpers.collection.IteratorUtil
import org.neo4j.tooling.GlobalGraphOperations
import org.neo4j.graphdb.Node
import spock.lang.Specification

class SampleNeo4jSpec extends Specification {

    /**
     * provide Neo4j stuff: graphDatabaseService and executionEngine
     */
    @Rule
    @Delegate
    Neo4jResource neo4jResource = new Neo4jResource( config: [execution_guard_enabled: "true"])

    GraphDatabaseService dummy


    def setup() {
        dummy = graphDatabaseService
    }

    def "graphDatabaseService is available"() {
        expect:
        graphDatabaseService != null  // N.B. due to @Delegate, we're accessing neo4jResource.graphDatabaseService
    }

    def "executionEngine is available"() {
        expect:
        executionEngine != null
    }

    def "by default a feature method has no transactional context"() {
        when: "trigger a write operation"
        graphDatabaseService.createNode()

        then:
        thrown(NotInTransactionException)
    }

    @WithNeo4jTransaction
    def "withNeo4jTransaction provides transactional"() {

        expect: "empty database"
        IteratorUtil.count(GlobalGraphOperations.at(graphDatabaseService).allNodes) == 0

        when:
        graphDatabaseService.createNode()

        then:
        notThrown NotInTransactionException
        IteratorUtil.count(GlobalGraphOperations.at(graphDatabaseService).allNodes) == 1
    }

    @WithNeo4jTransaction(field = "dummy")
    def "withNeo4jTransaction works with a field parameter"() {

        expect: "empty database"
        IteratorUtil.count(GlobalGraphOperations.at(graphDatabaseService).allNodes) == 0

        when:
        graphDatabaseService.createNode()

        then:
        notThrown NotInTransactionException
        IteratorUtil.count(GlobalGraphOperations.at(graphDatabaseService).allNodes) == 1
    }


    def "cypher method applied to String class"() {
        when:
        def executionResult = "create (n) return n".cypher()

        then:
        executionResult[0].n instanceof Node
    }

    def "parameterized cypher works on String class"() {
        setup:
        "create (n:Person {props})".cypher(props: [name: 'Stefan'])

        when:
        def executionResult = "match (n:Person) where n.name={name} return n.name as name".cypher(name:'Stefan')

        then:
        executionResult[0].name == 'Stefan'
    }
}
