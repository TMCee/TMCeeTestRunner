
package fi.helsinki.cs.tmc.testrunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestCaseList extends ArrayList<TestCase> {
    
    public TestCaseList findByFunctionName(String functionName) {
        TestCaseList result = new TestCaseList();
        for (TestCase t : this) {
            if (t.functionName.equals(functionName)) {
                result.add(t);
            }
        }
        return result;
    }

    public TestCaseList findByPointName(String pointName) {
        TestCaseList result = new TestCaseList();
        for (TestCase t : this) {
            if (Arrays.asList(t.pointNames).contains(pointName)) {
                result.add(t);
            }
        }
        return result;
    }
    
    public void writeToJsonFile(File file) throws IOException {
        Writer w = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), "UTF-8");
        writeToJson(w);
        w.close();
    }
    
    private void writeToJson(Writer w) throws IOException {
        Gson gson = new GsonBuilder().create();
        gson.toJson(this, w);
    }

    @Override
    public TestCaseList clone() {
        TestCaseList clone = new TestCaseList();

        for (TestCase t : this) {
            clone.add(new TestCase(t));
        }

        return clone;
    }

}
