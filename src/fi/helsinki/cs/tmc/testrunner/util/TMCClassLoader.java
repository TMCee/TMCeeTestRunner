package fi.helsinki.cs.tmc.testrunner.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class TMCClassLoader extends URLClassLoader {

    public TMCClassLoader(URL[] urls) {
        super(urls);
    }

    public static TMCClassLoader fromPath(String classPath)
            throws MalformedURLException {
        File myFile = new File(classPath);
        URL[] urls = { myFile.toURI().toURL() };
        return new TMCClassLoader(urls);
    }
}
