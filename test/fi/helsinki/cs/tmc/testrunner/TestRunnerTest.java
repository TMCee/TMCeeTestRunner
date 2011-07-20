package fi.helsinki.cs.tmc.testrunner;

import java.util.ArrayList;
import java.util.TreeMap;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestRunnerTest {
    @Test
    public void shouldReturnTestResultsSortedByAnnotation() throws Exception {
        TestRunner runner = new TestRunner(TestRunnerTestSubject.class);
        TreeMap<String, ArrayList<TestCase>> results = runner.runTests(5000);

        assertTrue(results.containsKey("one"));
        assertTrue(results.containsKey("two"));
        assertTrue(results.containsKey("three"));
        assertEquals("successfulTestCaseForOneTwoThree", results.get("one").get(0).methodName);
        assertEquals(TestCase.TEST_PASSED, results.get("one").get(0).status);

        assertEquals("successfulTestCaseForOneTwoThree", results.get("two").get(0).methodName);
        assertEquals(TestCase.TEST_PASSED, results.get("two").get(0).status);
        assertEquals("successfulTestCaseForTwo", results.get("two").get(1).methodName);
        assertEquals(TestCase.TEST_FAILED, results.get("two").get(1).status);
    }

    @Test
    public void shouldTimeout() throws Exception {
        TestRunner runner = new TestRunner(TimeoutTestSubject.class);
        TreeMap<String, ArrayList<TestCase>> results = runner.runTests(1000);

        assertTrue(results.containsKey("one"));
        assertTrue(results.containsKey("two"));

        TestCase infiniteCase = results.get("two").get(0);

        assertEquals("infiniteTwo", infiniteCase.methodName);
        assertEquals(TestCase.TEST_FAILED, infiniteCase.status);
        assertTrue(infiniteCase.message.contains("timeout"));
    }
}
