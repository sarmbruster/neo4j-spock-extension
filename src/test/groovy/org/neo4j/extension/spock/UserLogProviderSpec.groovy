package org.neo4j.extension.spock

import org.junit.Rule
import org.neo4j.kernel.impl.proc.Procedures
import org.neo4j.logging.AssertableLogProvider
import spock.lang.Specification

import static org.neo4j.logging.AssertableLogProvider.inLog

class UserLogProviderSpec extends Specification {

    /**
     * provide Neo4j stuff: graphDatabaseService and executionEngine
     */
    @Rule
    @Delegate(interfaces=false)
    Neo4jResource neo4jResource = new Neo4jResource(
            config: [execution_guard_enabled: "true"],
            userLogProvider: new AssertableLogProvider(),
            internalLogProvider: new AssertableLogProvider()  // NB: for a unknown reason we need to customize internalLogProvider as well
    )

    def "procedure doing logging can be asserted"() {
        when:
        "call org.neo4j.extension.spock.echoWithLog('magic')".cypher().close()

        then:
        userLogProvider.assertExactly(inLog(Procedures).info("magic"))
    }
}
