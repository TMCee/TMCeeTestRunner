/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.ctestrunner;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rase
 */
public class ValgrindMemoryTester {

    public static void analyzeMemory(Collection<Test> tests) {
        for (Test t : tests) {
            failTestWithMemoryError(t);
            if (t.isCheckedForMemoryLeaks()) failLeakingTest(t);
            if (t.isCheckedForMemoryUsage()) failTestUsingExcessMemory(t);
        }
    }

    private static void failTestWithMemoryError(Test test) {
        if (!test.getValgrindTrace().contains("ERROR SUMMARY: 0 errors") && test.getStatus().equals(Test.Status.PASSED)) {
            test.setStatus(Test.Status.FAILED);
            test.setMessage("Unit tests passed, but a memory error was detected by Valgrind. Please refer to the valgrind trace for more details.");
        }
    }
    
    private static void failLeakingTest(Test test) {
        String line = findLine(test.getValgrindTrace(), "definitely lost");
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(line);
        if (m.find()) {
            m.find();
            String s = m.group(); // Second group is lost bytes
            try {
                int leakedBytes = Integer.parseInt(s);
                if (leakedBytes > 0 && test.getStatus().equals(Test.Status.PASSED)) {
                    test.setStatus(Test.Status.FAILED);
                    test.setMessage("Unit tests passed, but a memory leak was detected. Please refer to the Valgrind trace for more details.");
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void failTestUsingExcessMemory(Test test) {
        // This might prove to be challenging. It seems that what you get back from the server is completely different from what you get locally
        String line = findLine(test.getValgrindTrace(), "bytes allocated");
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(line);
        if (m.find()) {
            m.find();
            m.find();
            m.find();
            String s = m.group(); // Fourth group is "bytes allocated"
            try {
                int usedAllocs = Integer.parseInt(s);
                if (usedAllocs > test.getMaxBytesAllocated() && test.getStatus().equals(Test.Status.PASSED)) {
                    test.setStatus(Test.Status.FAILED);
                    test.setMessage("Unit tests passed, but too much memory was used. Refer to the exercise description.");
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static String findLine(String content, String contains) {
        String line = "";
        for (String l : content.split("\n")) {
            if (l.contains(contains)) {
                line = l;
            }
        }
        return line;
    }
}
