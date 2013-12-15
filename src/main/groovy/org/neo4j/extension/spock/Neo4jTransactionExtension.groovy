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

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FeatureInfo

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
