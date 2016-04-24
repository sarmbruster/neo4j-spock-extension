package org.neo4j.extension.spock

import org.junit.Rule
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Label
import org.neo4j.graphdb.NotInTransactionException
import org.neo4j.helpers.collection.Iterables
import org.neo4j.kernel.impl.proc.Procedures
import org.neo4j.kernel.internal.GraphDatabaseAPI
import org.neo4j.graphdb.Node
import spock.lang.Specification

class SampleNeo4jSpec extends Specification {

    /**
     * provide Neo4j stuff: graphDatabaseService and executionEngine
     */
    @Rule
    @Delegate(interfaces=false)
    Neo4jResource neo4jResource = new Neo4jResource( config: [execution_guard_enabled: "true"])

    GraphDatabaseService dummy

    def setup() {
        dummy = graphDatabaseService
    }

    def "graphDatabaseService is available"() {
        expect:
        graphDatabaseService != null  // N.B. due to @Delegate, we're accessing neo4jResource.graphDatabaseService
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
        Iterables.count(graphDatabaseService.allNodes) == 0

        when:
        graphDatabaseService.createNode()

        then:
        notThrown NotInTransactionException
        Iterables.count(graphDatabaseService.allNodes) == 1
    }

    @WithNeo4jTransaction(field = "dummy")
    def "withNeo4jTransaction works with a field parameter"() {

        expect: "empty database"
        Iterables.count(graphDatabaseService.allNodes) == 0

        when:
        graphDatabaseService.createNode()

        then:
        notThrown NotInTransactionException
        Iterables.count(graphDatabaseService.allNodes) == 1
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

    def "check long and int"() {
        setup:
        "create (n:Person{value:1357016400000})".cypher()
        "create (n:Person{value:toInt(1)})".cypher()

        when:
        Neo4jUtils.withSuccessTransaction(graphDatabaseService) {
            def n = graphDatabaseService.createNode(Label.label("Person"))
            n.setProperty("value", 1357016400000 as int)
        }
        Neo4jUtils.withSuccessTransaction(graphDatabaseService) {
            for (def n: graphDatabaseService.findNodes(Label.label("Person"))) {
                println "id: ${n.id}, val: ${n.getProperty("value")}, type: ${n.getProperty("value").class}"
            }
        }

        then:
        true

        when:
        def result = "match(n:Person) where n.value>1357016300000 return n".cypher()

        then:
        result.size() == 1

        when:
        result = "match(n:Person) where n.value>1357016400001 return n".cypher()

        then:
        result.size() == 0

        when:
        result = "match(n:Person) where n.value>={v} return n".cypher(v:1357016300000)

        then:
        result.size() == 1

        when:
        result = "match(n:Person) where n.value>={v} return n".cypher(v:1357016300000L)

        then:
        result.size() == 1
    }

    def "procedures defined in non-jar parts of classpath are loaded automatically"() {
        when:
        def procedures = ((GraphDatabaseAPI)graphDatabaseService).dependencyResolver.resolveDependency(Procedures)
        def nonSystemProcedures = procedures.all.grep { !(it.name().namespace()[0] in ["db" ,"sys"]) }

        then: "some procedures from this project have been loaded"
        !nonSystemProcedures.empty

    }
}
