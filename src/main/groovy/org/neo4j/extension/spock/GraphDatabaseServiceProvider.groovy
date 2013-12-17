package org.neo4j.extension.spock

import org.neo4j.graphdb.GraphDatabaseService

interface GraphDatabaseServiceProvider {
    public GraphDatabaseService getGraphDatabaseService()
}
