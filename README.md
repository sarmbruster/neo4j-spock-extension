# neo4j-spock-helper #

## purpose ##
provide a convenience jar file to be used for testing [Neo4j](http://www.neo4j.org) applications using [Spock](http://www.spockframework.org).

## license ##
GPL v3

## build & install ##
neo4j-spock-helper uses [Gradle](http://www.gradle.org) as build system.

### features ###
* for unit tests, derive from NeoSpecification instead of spock.lang.Specification
 * provides a graphDatbaseService, executionEngine and a transaction
 * features CypherMixin which allows you to use a cypher method directly on a string, e.g. "match (n) return n".cypher()

* for integration tests, derive from NeoServerSpecification
 * look at NeoServerSpecifcationSpec to see a usage example