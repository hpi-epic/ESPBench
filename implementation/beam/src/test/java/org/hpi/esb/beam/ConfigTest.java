package org.hpi.esb.beam;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigTest {

    private static final String DEFAULT_RUNNER = "spark";

    @Test
    public void testSuccessfullyReadConfig() {
        String runner = Config.get(Config.SYSTEM_PROPERTY);
        assertNotNull(runner);
        assertEquals(runner, DEFAULT_RUNNER);
        assertNotEquals(runner, DEFAULT_RUNNER + "FCM");
    }
}