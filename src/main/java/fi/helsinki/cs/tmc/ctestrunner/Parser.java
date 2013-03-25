package fi.helsinki.cs.tmc.ctestrunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author rase
 */
public class Parser {

    private File testOutput;
    private File testPoints;
    private File valgrindTraces;
    private File memoryOptions;
    private HashMap<String, Test> tests = new HashMap<String, Test>();
    private HashMap<String, TestSuite> testSuites = new HashMap<String, TestSuite>();

    public Parser(File testOutput, File testPoints, File valgrindTraces, File memoryOptions) {
        this.testOutput = testOutput;
        this.testPoints = testPoints;
        this.valgrindTraces = valgrindTraces;
        this.memoryOptions = memoryOptions;
    }

    public TestList getTests() {
        TestList testList = new TestList();
        for (Test test : tests.values()) {
            testList.add(test);
        }
        return testList;
    }

    public void parse() {
        try {
            System.out.println("Parsing tests");
            tests = parseTests(testOutput);
            testSuites = parseTestSuites(testOutput);
            System.out.println("Parsing points");
            addPoints(testPoints, tests, testSuites);
            System.out.println("Parsing valgrind trace");
            addValgrindOutput(valgrindTraces, new ArrayList<Test>(tests.values()));
            if (memoryOptions != null) {
                System.out.println("Parsing memory tests");
                addMemoryTests();
                System.out.println("Applying memory tests");
                ValgrindMemoryTester.analyzeMemory(tests.values());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, TestSuite> parseTestSuites(File testOutput) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(testOutput);

        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("suite");
        HashMap<String, TestSuite> suites = new HashMap<String, TestSuite>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element node = (Element) nodeList.item(i);
            String name = node.getElementsByTagName("title").item(0).getTextContent();
            suites.put(name, new TestSuite(name));
        }
        return suites;
    }

    private void addMemoryTests() throws FileNotFoundException {
        HashMap<String, String> memoryInfoByName = new HashMap<String, String>();
        Scanner scanner = new Scanner(memoryOptions, "UTF-8");
        while (scanner.hasNextLine()) {
            String[] split = scanner.nextLine().split(" ");
            memoryInfoByName.put(split[0], split[1] + " " + split[2]);
        }
        scanner.close();

        for (Test t : tests.values()) {
            String str = memoryInfoByName.get(t.getName());
            if (str == null) {
                continue;
            }
            String[] params = str.split(" ");
            int checkLeaks, maxBytes;
            try {
                checkLeaks = Integer.parseInt(params[0]);
                maxBytes = Integer.parseInt(params[1]);
            } catch (Exception e) {
                checkLeaks = 0;
                maxBytes = -1;
            }
            t.setMaxBytesAllocated(maxBytes);
            t.setCheckedForMemoryLeaks(checkLeaks == 1);
        }
    }

    private HashMap<String, Test> parseTests(File testOutput) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(testOutput);

        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("test");
        HashMap<String, Test> tests = new HashMap<String, Test>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element node = (Element) nodeList.item(i);
            String result = node.getAttribute("result");
            String name = node.getElementsByTagName("description").item(0).getTextContent();
            String message = node.getElementsByTagName("message").item(0).getTextContent();
            tests.put(name, new Test(name, (result.equals("success") ? Test.Status.PASSED : Test.Status.FAILED), message));
        }

        return tests;
    }

    private void addPoints(File testPoints, HashMap<String, Test> tests, HashMap<String, TestSuite> testSuites) throws FileNotFoundException {
        Scanner scanner = new Scanner(testPoints, "UTF-8");
        while (scanner.hasNextLine()) {
            String[] splitLine = scanner.nextLine().split(" ");
            if (splitLine[0].equals("[test]")) {
                String name = splitLine[1];
                Test associatedTest = tests.get(name);
                if (associatedTest != null) {
                    String[] pointsArray = new String[splitLine.length - 2];
                    System.arraycopy(splitLine, 2, pointsArray, 0, pointsArray.length);
                    associatedTest.setPoints(pointsArray);
                }
            } else if (splitLine[0].equals("[suite]")) {
                String name = splitLine[1];
                String[] pointsArray = new String[splitLine.length - 2];
                System.arraycopy(splitLine, 2, pointsArray, 0, pointsArray.length);
                TestSuite associatedSuite = testSuites.get(name);
                if (associatedSuite != null) {
                    associatedSuite.setPoints(pointsArray);
                }
            } else {
                // Do nothing at least of for now
            }

        }
        scanner.close();
    }

    private void addValgrindOutput(File outputFile, ArrayList<Test> tests) throws FileNotFoundException {
        Scanner scanner = new Scanner(outputFile, "UTF-8");
        String parentOutput = ""; // Contains total amount of memory used and such things. Useful if we later want to add support for testing memory usage
        String[] outputs = new String[tests.size()];
        int[] pids = new int[tests.size()];
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = "";
        }

        String line = scanner.nextLine();
        int firstPID = parsePID(line);
        parentOutput += "\n" + line;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            int pid = parsePID(line);
            if (pid == -1) {
                continue;
            }
            if (pid == firstPID) {
                parentOutput += "\n" + line;
            } else {
                outputs[findIndex(pid, pids)] += "\n" + line;
            }
        }
        scanner.close();

        for (int i = 0; i < outputs.length; i++) {
            tests.get(i).setValgrindTrace(outputs[i]);
        }
    }

    private int findIndex(int pid, int[] pids) {
        for (int i = 0; i < pids.length; i++) {
            if (pids[i] == pid) {
                return i;
            }
            if (pids[i] == 0) {
                pids[i] = pid;
                return i;
            }
        }
        return 0;
    }

    private int parsePID(String line) {
        try {
            return Integer.parseInt(line.split(" ")[0].replaceAll("(==|--)", ""));
        } catch (Exception e) {
            return -1;
        }
    }
}