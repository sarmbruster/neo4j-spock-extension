package org.neo4j.extension.spock

import org.junit.rules.ExternalResource
import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.server.NeoServer
import org.neo4j.test.server.HTTP

/**
 * abstract base class for spock tests using a Neo4j server
 */
class Neo4jServerResource extends ExternalResource {

    GraphDatabaseService graphDatabaseService
    NeoServer server
    int port
    def thirdPartyJaxRsPackages = [:]
    def config = [:]
    String baseUrl
    @Lazy
    ExecutionEngine executionEngine = new ExecutionEngine(graphDatabaseService)

    @Override
    protected void before() throws Throwable {
        port = findFreePort()
        def serverBuilder = new ConfigurableServerBuilder()

        thirdPartyJaxRsPackages.each { packageName, mountPoint ->
            serverBuilder.withThirdPartyJaxRsPackage(packageName, mountPoint)
        }
        config.each { k, v ->
            serverBuilder.withConfigProperty(k, v)
        }

        server = serverBuilder.onPort(port).build();
        server.start()
        baseUrl = server.baseUri().toASCIIString()
        graphDatabaseService = server.database.graph
        String.metaClass.cypher = { -> executionEngine.execute(delegate) }
        String.metaClass.cypher = { Map params -> executionEngine.execute(delegate, params) }
    }

    @Override
    protected void after() {
        server.stop()
    }

    int findFreePort() {
        ServerSocket server = new ServerSocket(0)
        int port = server.localPort
        server.close()
        port
    }

    def withTransaction(Closure closure) {
        Neo4jUtils.withSuccessTransaction(graphDatabaseService, closure)
    }

    def getHttp() {
        HTTP.withBaseUri(baseUrl)
    }

}
