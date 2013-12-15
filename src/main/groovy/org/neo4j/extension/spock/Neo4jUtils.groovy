/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
