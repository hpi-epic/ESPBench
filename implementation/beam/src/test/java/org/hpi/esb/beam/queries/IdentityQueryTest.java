package org.hpi.esb.beam.queries;

import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.NeedsRunner;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.hpi.esb.beam.schema.SensorRecord;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IdentityQueryTest {

    private static int baseVal = 20;
    private static int val1 = 0;
    private static int val2 = baseVal;
    private static int val3 = 2 * baseVal;

    private static final String[] RECORD_LIST = new String[]{
            new SensorRecord(0, 10, "" + val1).serialize(),
            new SensorRecord(1, 11, "" + val2).serialize(),
            new SensorRecord(2, 12, "" + val3).serialize()
    };

    private static final List<String> RECORDS = Arrays.asList(RECORD_LIST);

    @Rule
    public final transient TestPipeline p = TestPipeline.create();

    @Test
    @Category(NeedsRunner.class)
    public void testReturningInputWithoutModification() {
        p.getCoderRegistry().registerCoderForClass(StringUtf8Coder.class, StringUtf8Coder.of());

        Create.Values<String> recordList = Create.of(RECORDS);
        PCollection<String> input = p.apply(recordList);
        PCollection<String> result = input.apply(new IdentityQuery());
        PAssert.that(result).containsInAnyOrder(RECORDS);
        p.run().waitUntilFinish();
    }


}
