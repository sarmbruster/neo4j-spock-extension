package org.neo4j.extension.spock;

import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.stream.Stream;

/**
 * @author Stefan Armbruster
 */
public class SampleProcedures {

    @Procedure
    public Stream<Output> echo(@Name("s") String s) {
        Stream.Builder<Output> builder = Stream.builder();
        builder.accept(new Output(s));
        return builder.build();
    }

    public class Output {
        public String output;

        private Output(String output) {
            this.output = output;
        }
    }
}
