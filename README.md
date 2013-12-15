[![Build Status](https://secure.travis-ci.org/sarmbruster/neo4j-spock-extension.png)](http://travis-ci.org/sarmbruster/neo4j-spock-extension)

# neo4j-spock-extension #

## purpose ##
provide a convenience jar file to be used for testing [Neo4j](http://www.neo4j.org) applications using [Spock](http://www.spockframework.org).

## license ##
[GPL v3](https://raw.github.com/sarmbruster/neo4j-spock-extension/master/LICENSE.txt)

## build & install ##
neo4j-spock-extension uses [Gradle](http://www.gradle.org) as build system. To build on your own, use `./gradlew test assemble`.

## usage ##

Depending on your project's used build system, use one of the alternatives below.

In case of Gradle, embed into your project's `build.gradle` the following dependency:

    testCompile group: 'org.neo4j.contrib', name: 'neo4j-spock-extension', version: '0.1'
    
If Maven is used:

    <dependency>
        <groupId>org.neo4j.contrib</groupId>
        <artifactId>neo4j-spock-extension</artifactId>
        <version>0.1</version>
        <scope>test</scope>
    </dependency>

### features ###

neo4j-spock-extension supports two different types of tests:

1. unit tests: these use internally a embedded Neo4j instance
1. server tests: these require a full Neo4j server

For unit tests, see [SampleNeo4jSpec](src/test/groovy/org/neo4j/extension/spock/SampleNeo4jSpec.groovy) as an example. The following noteworthy features:
 * a Neo4jResource is used with a @Rule and @Delegate annotation. Neo4jResource creates a new Neo4j embedded instance for each test case. Note that internally Neo4j's TestGraphDatabaseFactory is used.
 * the optional constructor argument for Neo4jResource might pass in config parameters used when creating the Neo4j instance
 * since @Delegate is used, any fields and methods exposed by Neo4jResource are directly accessible 
 * Neo4jResource exposes a [graphDatabaseService](http://api.neo4j.org/current/org/neo4j/graphdb/GraphDatabaseService.html) and an [executionEngine](http://api.neo4j.org/current/org/neo4j/cypher/javacompat/ExecutionEngine.html) to be used in your test cases.
 * by default, there is no transactional context spawned for your test methods
 * if a test case is annotated with @WithNeo4jTransaction, a transactional context is spawned
 * the String class is enriched by a `cypher` method. The `cypher` method can be used with and without parameters:


    "MATCH (n) RETURN n LIMIT 10".cypher()
    "MATCH (n) RETURN n LIMIT {limit}".cypher(limit:10  )

For server tests, see [SampleNeo4jServerSpec](src/test/groovy/org/neo4j/extension/spock/SampleNeo4jServerSpec.groovy).
