/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.ctestrunner;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author rase
 */
public class TestList extends ArrayList<Test> {

    public TestList findByFunctionName(String functionName) {
        TestList result = new TestList();
        for (Test t : this) {
            if (t.getName().equals(functionName)) {
                result.add(t);
            }
        }
        return result;
    }

    public TestList findByPointName(String pointName) {
        TestList result = new TestList();
        for (Test t : this) {
            if (Arrays.asList(t.getPoints()).contains(pointName)) {
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
        FieldNamingStrategy namingStrategy = new FieldNamingStrategy() {

            public String translateName(Field field) {
                if (field.getName().equals("name")) return "methodName";
                return field.getName();
            }
        };

        ExclusionStrategy exclusionStrategy = new ExclusionStrategy() {

            public boolean shouldSkipField(FieldAttributes fa) {
                if (fa.getName().equals("checkedForMemoryLeaks") || fa.getName().equals("maxBytesAllocated")) return true;
                return false;
            }

            public boolean shouldSkipClass(Class<?> type) {
                return false;
            }
        };
        
        Gson gson = new GsonBuilder().setFieldNamingStrategy(namingStrategy).setExclusionStrategies(exclusionStrategy).create();
        gson.toJson(this, w);
    }

    @Override
    public TestList clone() {
        TestList clone = new TestList();

        for (Test t : this) {
            clone.add(new Test(t));
        }

        return clone;
    }
}
