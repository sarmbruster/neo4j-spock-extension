package org.neo4j.extension.spock;

import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.stream.Stream;

/**
 * @author Stefan Armbruster
 */
public class SampleProcedures {

    @Context
    public Log log;

    @Procedure
    public Stream<Output> echo(@Name("s") String s) {
        Stream.Builder<Output> builder = Stream.builder();
        builder.accept(new Output(s));
        return builder.build();
    }

    @Procedure
    public Stream<Output> echoWithLog(@Name("s") String s) {
        log.info(s);
        return echo(s);
    }

    public class Output {
        public String output;

        private Output(String output) {
            this.output = output;
        }
    }
}
