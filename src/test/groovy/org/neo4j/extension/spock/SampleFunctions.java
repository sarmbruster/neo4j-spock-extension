package org.neo4j.extension.spock;

import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

/**
 * @author Stefan Armbruster
 */
public class SampleFunctions {

    @UserFunction
    public String echo(@Name("s") String s) {
        return s;
    }

}
