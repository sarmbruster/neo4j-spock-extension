/*
 * Copyright 2014 the original author or authors.
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

import org.junit.ClassRule
import org.neo4j.extension.spock.thirdpartyjaxrs.LifeCycles
import org.neo4j.extension.spock.thirdpartyjaxrs.extension1.LifeCycle1
import org.neo4j.extension.spock.thirdpartyjaxrs.extension2.LifeCycle2
import org.neo4j.server.AbstractNeoServer
import org.neo4j.server.web.WebServer
import spock.lang.Shared
import spock.lang.Specification

class ExtensionLoadOrder1Spec extends Specification {

    /**
     * we need to define our test resources: a neo4j server
     * two options:
     * <ol>
     *     <li>one server per test class: use @Shared and @ClassRule</li>
     *     <li>one server per test method: use @Rule and optionally @Delegate
     * </ol>
     * N.B. @Delegate and @Shared cannot be combined!
     */
    @Shared
    @ClassRule
    Neo4jServerResource neo4j = new Neo4jServerResource(
            thirdPartyJaxRsPackages: [
                                "org.neo4j.extension.spock.thirdpartyjaxrs.extension1": "/mount1",
                                "org.neo4j.extension.spock.thirdpartyjaxrs.extension2": "/mount2",
                        ]
    )

    def "extensions are initialized in correct order"() {
        expect: "last two invocations match"
        LifeCycles.lifeCyclesExecutions[-2..-1] == [ LifeCycle1, LifeCycle2]
    }

    def "check if redirects get resolved"() {
        when:
        def response = neo4j.http.GET("mount1")

        then: "we don't get a redirect"
        response.status() != 302

        and:
        response.status() == 200
        response.rawContent() == "test"


    }

}
