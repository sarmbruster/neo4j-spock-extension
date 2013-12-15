package org.neo4j.extension.spock

import org.neo4j.graphdb.GraphDatabaseService

abstract class Neo4jUtils {

    static withSuccessTransaction(GraphDatabaseService graphDatabaseService, Closure closure) {
        withTransaction(graphDatabaseService, closure, true)
    }

    static withRollbackTransaction(GraphDatabaseService graphDatabaseService, Closure closure) {
        withTransaction(graphDatabaseService, closure, false)
    }

    static withTransaction(GraphDatabaseService graphDatabaseService, Closure closure, boolean success = false) {
        def tx = graphDatabaseService.beginTx()
        try {
            def result = closure.call()
            if (success) {
                tx.success()
            }
            result
        } finally {
            tx.close()
        }
    }

}
