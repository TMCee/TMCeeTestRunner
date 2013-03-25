/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.ctestrunner;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author rase
 */
public class Main {
    private String checkRootDir;
    private String checkResultsFilename;
    private String checkAvailablePointsFilename;
    private String checkValgrindOutputFilename;
    private String memoryTestOutputFilename;
    private String outputFilename;
    
    public static void main(String[] args) {
        try {
            new Main().run();
        } catch (Throwable t) {
            System.err.print("Uncaught exception in main thread.");
            t.printStackTrace(System.err);
        }
    }
    
    public void run() throws IOException {
        // Let's assume code is already compiled
        // This is done by tmc-run script which is passed to the sandbox with each submission
        try {
            System.out.println("Reading properties");
            readProperties();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.out.println();
            System.exit(1337);
        }
        File memoryTestFile = new File(memoryTestOutputFilename);
        if (!memoryTestFile.exists()) memoryTestFile = null;
        System.out.println("Initializing parser");
        Parser parser = new Parser(
                new File(checkResultsFilename),
                new File(checkAvailablePointsFilename),
                new File(checkValgrindOutputFilename),
                memoryTestFile);
        System.out.println("Parsing tests");
        parser.parse();
        System.out.println("Tests parsed");
        TestList tests = parser.getTests();
        System.out.println("Writing test results");
        writeResults(tests);
        System.out.println("Done!");
    }
    
    private void readProperties() {
        checkRootDir = requireProperty("tmc.check_root_dir");
        checkResultsFilename = requireProperty("tmc.check_results_file");
        checkAvailablePointsFilename = requireProperty("tmc.check_available_points");
        checkValgrindOutputFilename = requireProperty("tmc.check_valgrind_output");
        memoryTestOutputFilename = requireProperty("tmc.memory_test_output");
        outputFilename = requireProperty("tmc.ctestrunner_output_file");
    }
    
    private String requireProperty(String name) {
        String prop = System.getProperty(name);
        if (prop != null) {
            return prop;
        } else {
            throw new IllegalArgumentException("Missing property: " + name);
        }
    }
    
    private void writeResults(TestList tests) throws IOException {
        tests.writeToJsonFile(new File(outputFilename));
    }
}
