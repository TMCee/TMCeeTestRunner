/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.testrunner;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author rase
 */
public class Main {
    private String checkFileDir;
    private String checkResultsFilename;
    
    public static void main(String[] args) {
        try {
            new Main().run();
        } catch (Throwable t) {
            System.err.print("Uncaught exception in main thread.");
            t.printStackTrace(System.err);
        }
    }
    
    public void run() throws IOException {
        // Run the code here
        try {
            readProperties();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.out.println();
            System.exit(1);
        }
        
        Parser parser = new Parser(
                new File("tmc_text_results.xml"),
                new File("tmc_available_points.txt"),
                new File("valgrind.log"));
        parser.parse();
        TestList tests = parser.getTests();
        writeResults(tests);
    }
    
    private void readProperties() {
        checkFileDir = requireProperty("tmc.test_class_dir");
        checkResultsFilename = requireProperty("tmc.results_file");
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
        tests.writeToJsonFile(new File(checkResultsFilename));
    }
}
