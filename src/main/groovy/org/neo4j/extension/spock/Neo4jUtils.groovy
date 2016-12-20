package org.neo4j.extension.spock

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.impl.proc.Procedures
import org.neo4j.kernel.impl.transaction.TransactionCounters
import org.neo4j.kernel.internal.GraphDatabaseAPI
import org.neo4j.procedure.Procedure
import org.neo4j.procedure.UserFunction
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder

import java.lang.annotation.Annotation

abstract class Neo4jUtils {

    static withSuccessTransaction(GraphDatabaseService graphDatabaseService, Closure closure) {
        withTransaction(graphDatabaseService, closure, true)
    }

    static withRollbackTransaction(GraphDatabaseService graphDatabaseService, Closure closure) {
        withTransaction(graphDatabaseService, closure, false)
    }

    static withTransaction(GraphDatabaseService graphDatabaseService, Closure closure, boolean success = false) {
        def tx = graphDatabaseService.beginTx()
        try {
            def result = closure.call()
            if (success) {
                tx.success()
            }
            result
        } finally {
            tx.close()
        }
    }

    static void assertNoOpenTransaction(GraphDatabaseService graphDatabaseService) {
        def resolver = ((GraphDatabaseAPI) graphDatabaseService).dependencyResolver
        def counters = resolver.resolveDependency(TransactionCounters)
        def active = counters.numberOfActiveTransactions
        def activeRead = counters.numberOfActiveReadTransactions
        def activeWrite = counters.numberOfActiveWriteTransactions
        if ((active != 0) || (activeRead != 0) || (activeWrite != 0)) {
            throw new IllegalStateException("there are open transactions! total: $active read: $activeRead write: $activeWrite")
        }
    }

    static Set<Class> findLocalClassesWithProcedureAnnotation(Class<? extends Annotation> annotation) {
        def cl = Thread.currentThread().contextClassLoader
        def nonJarUrls = ((URLClassLoader) cl).getURLs().grep {
            def url = it.toString()
            [".jar", ".pom"].every { postfix -> !url.endsWith(postfix)}
        }

        def reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(nonJarUrls)
                .setScanners(new MethodAnnotationsScanner())
                .filterInputsBy(new FilterBuilder().include(".*"))
        );
        def resources = reflections.getMethodsAnnotatedWith(annotation);
        resources.collect { it.declaringClass } as Set // remove duplicates
    }

    static void registerLocalClassesWithProcedureOrUserFunctionAnnotation(GraphDatabaseService graphDatabaseService) {
        Procedures procedures = ((GraphDatabaseAPI)graphDatabaseService).dependencyResolver.resolveDependency(Procedures)
        for (Class clazz in findLocalClassesWithProcedureAnnotation(Procedure)) {
            procedures.registerProcedure(clazz)
        }
        for (Class clazz in findLocalClassesWithProcedureAnnotation(UserFunction)) {
            procedures.registerFunction(clazz)
        }
    }
}
