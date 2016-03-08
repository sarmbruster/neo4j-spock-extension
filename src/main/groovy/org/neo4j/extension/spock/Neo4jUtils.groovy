package org.neo4j.extension.spock

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.impl.transaction.TransactionCounters
import org.neo4j.kernel.internal.GraphDatabaseAPI

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

    static void assertNoOpenTransaction(GraphDatabaseService graphDatabaseService) {
        def resolver = ((GraphDatabaseAPI) graphDatabaseService).dependencyResolver
        def counters = resolver.resolveDependency(TransactionCounters)
        def active = counters.numberOfActiveTransactions
        def activeRead = counters.numberOfActiveReadTransactions
        def activeWrite = counters.numberOfActiveWriteTransactions
        if ((active != 0) || (activeRead != 0) || (activeWrite != 0)) {
            throw new IllegalStateException("there are open transactions! total: $active read: $activeRead write: $activeWrite")
        }
    }


}
