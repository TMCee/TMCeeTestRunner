package fi.helsinki.cs.tmc.testrunner;

import static fi.helsinki.cs.tmc.testrunner.TestCase.Status.*;

public class TestCase {
    public enum Status {
        PASSED, FAILED, NOT_RUN
    }
    
    public String suiteName;
    public String functionName;
    public String[] pointNames;
    public String message;
    public Status status;
    public String valgrindOutput;

    public TestCase(String className, String methodName, String[] pointNames) {
        this.functionName = methodName;
        this.suiteName = className;
        this.status = NOT_RUN;
        this.pointNames = pointNames;
        this.message = null;
        this.valgrindOutput = null;
    }

    public TestCase(TestCase aTestCase) {
        this.functionName = aTestCase.functionName;
        this.suiteName = aTestCase.suiteName;
        this.message = aTestCase.message;
        this.status = aTestCase.status;
        this.pointNames = aTestCase.pointNames.clone();
    }

    public void testFinished() {
        if (this.status != FAILED) {
            this.status = PASSED;
        }
    }

//    public void testFailed(Failure f) {
//        this.message = failureMessage(f);
//        this.status = FAILED;
//        
//        Throwable ex = f.getException();
//        if (ex != null) {
//            this.exception = new CaughtException(ex);
//        }
//    }
    
//    private String failureMessage(Failure f) {
//        if (f.getException() == null) { // Not sure if this is possible
//            return null;
//        }
//        
//        String exceptionClass = f.getException().getClass().getSimpleName();
//        String exMsg = f.getException().getMessage();
//        if (exceptionClass.equals("AssertionError")) {
//            if (exMsg != null) {
//                return exMsg;
//            } else {
//                return exceptionClass;
//            }
//        } else {
//            if (exMsg != null) {
//                return exceptionClass + ": " + exMsg;
//            } else {
//                return exceptionClass;
//            }
//        }
//    }

    @Override
    public String toString() {
        String ret = this.functionName + " (" + this.suiteName + ") " + status;
        if (this.message != null) {
            ret += ": " + this.message;
        }            
        return ret;
    }
}
