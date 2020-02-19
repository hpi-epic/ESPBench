package org.hpi.esb.beam.schema;

import org.apache.beam.sdk.coders.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StatisticsAggregatorCoder extends AtomicCoder<StatisticsAggregator> {

    static final long serialVersionUID = 1965L;

    private final Coder<String> STRING_CODER = StringUtf8Coder.of();

    @Override
    public void encode(StatisticsAggregator value, OutputStream outStream) throws CoderException, IOException {
        STRING_CODER.encode(value.extractOutput(), outStream);
    }

    @Override
    public StatisticsAggregator decode(InputStream inStream) throws CoderException, IOException {
        Coder<Long> LONG_CODER = BigEndianLongCoder.of();
        StatisticsAggregator aggregator = new StatisticsAggregator();
        long value = (LONG_CODER.decode(inStream));
        aggregator.addInput(value);
        return aggregator;
    }
}
