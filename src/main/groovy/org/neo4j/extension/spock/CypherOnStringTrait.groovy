package org.neo4j.extension.spock

/**
 * provide `cypher()` method on Strings and keep track of results to provide a hook to close them
 * @author Stefan Armbruster
 */
trait CypherOnStringTrait implements GraphDatabaseServiceProvider {

    private List cypherResults = []

    public void initCypherOnString() {
        String.metaClass.cypher = { ->
            addResultToList(graphDatabaseService.execute(getDelegate())) // simple delegate does not work here
        }
        String.metaClass.cypher = { Map params ->
            addResultToList(graphDatabaseService.execute(getDelegate(), params)) // simple delegate does not work here
        }
    }

    //cannot use private here
    def addResultToList(def result) {
        cypherResults << result
        result
    }

    public void closeCypher() {
        cypherResults.each { it.close() }
        cypherResults = []
    }

}