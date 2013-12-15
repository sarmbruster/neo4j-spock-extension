package org.neo4j.extension.spock

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
                Neo4jResource value = invocation.instance.properties.values().find { it instanceof Neo4jResource}
                assert value, "no Neo4jResource defined in specification ${invocation.instance.class.name}"
                Neo4jUtils.withSuccessTransaction(value.graphDatabaseService, { invocation.proceed() } )
            }
        })
    }

}
