package org.neo4j.extension.spock

import org.neo4j.graphdb.GraphDatabaseService
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FeatureInfo

/**
 * provide transactional context when test method is annotated with {@link WithNeo4jTransaction}
 * as a requirement, the test class needs to have a field of type {@link Neo4jResource}
 */
class Neo4jTransactionExtension extends AbstractAnnotationDrivenExtension<WithNeo4jTransaction> {

    @Override
    void visitFeatureAnnotation(WithNeo4jTransaction annotation, FeatureInfo feature) {
        feature.featureMethod.addInterceptor(new AbstractMethodInterceptor() {
            @Override
            void interceptFeatureMethod(IMethodInvocation invocation) throws Throwable {

                GraphDatabaseService graphDatabaseService = null

                def field = annotation.field()
                if (!field ) {

                    graphDatabaseService = invocation.instance.properties.values().find {
                        it instanceof GraphDatabaseService
                    }

                    if (graphDatabaseService == null) {
                        GraphDatabaseServiceProvider value = invocation.instance.properties.values().find {
                            it instanceof GraphDatabaseServiceProvider
                        }
                        assert value, "no property of type GraphDatabaseService or Neo4jResource is defined in specification ${invocation.instance.class.name}"
                        graphDatabaseService = value.graphDatabaseService
                    }
                } else {
                    graphDatabaseService = invocation.instance.properties[field]
                }
                Neo4jUtils.withSuccessTransaction(graphDatabaseService, { invocation.proceed() } )
            }
        })
    }

}
