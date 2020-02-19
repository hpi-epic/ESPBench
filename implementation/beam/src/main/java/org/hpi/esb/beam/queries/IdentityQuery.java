package org.hpi.esb.beam.queries;

import org.apache.beam.sdk.values.PCollection;

public class IdentityQuery extends Query {

    static final long serialVersionUID = 1965L;

    @Override
    public PCollection<String> expand(PCollection<String> input) {
        return input;
    }

}
