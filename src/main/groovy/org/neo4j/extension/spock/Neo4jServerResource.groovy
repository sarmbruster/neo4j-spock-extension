package org.neo4j.extension.spock

import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.config.ClientConfig
import com.sun.jersey.api.client.config.DefaultClientConfig
import org.junit.rules.ExternalResource
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.harness.ServerControls
import org.neo4j.harness.TestServerBuilder
import org.neo4j.harness.TestServerBuilders
import org.neo4j.server.NeoServer
import org.neo4j.test.server.HTTP

import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * abstract base class for spock tests using a Neo4j server
 */
class Neo4jServerResource extends ExternalResource implements CypherOnStringTrait {

    GraphDatabaseService graphDatabaseService
    ServerControls controls
    NeoServer server
    def thirdPartyJaxRsPackages = [:]
    def config = [:]
    String baseUrl
    URI baseURI
    boolean shouldAutoRegisterProcedures = true

    @Override
    protected void before() throws Throwable {

        TestServerBuilder builder = TestServerBuilders.newInProcessBuilder()

        thirdPartyJaxRsPackages.each { packageName, mountPoint ->
            builder.withExtension(mountPoint, packageName)
        }
        config.each { k, v ->
            builder.withConfig(k,v)
        }

        controls = builder.newServer()

        baseURI = controls.httpURI()
        baseUrl = baseURI.toASCIIString()
        server = controls.server
        graphDatabaseService = server.database.graph

        if (shouldAutoRegisterProcedures) {
            Neo4jUtils.registerLocalClassesWithProcedureOrUserFunctionAnnotation(graphDatabaseService)
        }

        initCypherOnString()
        fixFollowRedirectsInHttp()
    }

    public void fixFollowRedirectsInHttp() {
// change a private static final (see http://stackoverflow.com/questions/3301635/change-private-static-final-field-using-java-reflection)
        Field field = HTTP.class.getDeclaredField("CLIENT")

        Field modifiersField = Field.class.getDeclaredField("modifiers")
        modifiersField.setAccessible(true)
        modifiersField.setInt(field, field.modifiers & ~Modifier.FINAL)
        field.setAccessible(true)

        DefaultClientConfig defaultClientConfig = new DefaultClientConfig();
        defaultClientConfig.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, Boolean.TRUE);
        field.set(null, Client.create(defaultClientConfig))
    }

    @Override
    protected void after() {
        closeCypher()
        Neo4jUtils.assertNoOpenTransaction(graphDatabaseService)
        controls.close()
    }

    def withTransaction(Closure closure) {
        Neo4jUtils.withSuccessTransaction(graphDatabaseService, closure)
    }

    def getHttp() {
        HTTP.withBaseUri(baseUrl)
    }

    def postLegacyCypher(json) {
        http.POST("db/data/cypher", json)
    }

    def postTransactionalCypher(statements, params=null) {
        http.POST("db/data/transaction/commit", createJsonForTransactionalEndpoint(statements, params))
    }

    /**
     * create a collection structure fitting being suitable for json format used for
     * transactional endpoint
     * @param statements array holding cypher statements
     * @param params array holding parameter for statements
     * @return
     */
    def createJsonForTransactionalEndpoint(statements, params) {
        if (!params) {
            params = statements.collect { [:] }
        }
        def transposed = [statements, params].transpose()
        [
                statements: transposed.collect { [statement: it[0], parameters: it[1]] }
        ]
    }
}
