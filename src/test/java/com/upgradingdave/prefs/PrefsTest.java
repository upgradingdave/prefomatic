package com.upgradingdave.prefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Calendar;

import junit.framework.AssertionFailedError;

import org.apache.commons.configuration.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.upgradingdave.prefs.PrefsHelper;

public class PrefsTest {
    public static String filePath;
    public static String dirPath;
    public static Configuration prefs;
    public static final String TEST_APP_NAME = "app1";

    @Before
    public void setUp() {
        String osEnvName = PrefsHelper.getOSEnvVariableName();
        assertEquals(osEnvName, "PREFOMATIC_TEST");
        String osEnvPath = System.getenv(osEnvName);
        
        dirPath = osEnvPath + "/" + TEST_APP_NAME;
        filePath = dirPath + "/" + TEST_APP_NAME + ".properties";

        // create directory
        boolean appDir = new File(dirPath).mkdir();
        assertTrue(appDir);
        
        File f = new File(filePath);
        assertTrue(!f.exists());

        try {
            FileWriter fstream = new FileWriter(filePath);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("only.defined.in.external=External Properties is working");
            out.newLine();
            out.write("defined.in.both.places=this is the external value");
            out.close();
        } catch (Exception e) {// Catch exception if any
            throw new AssertionFailedError(
                    "For this test to run, you must create an environment variable called 'PREFOMATIC_TEST' and point it to the test/resources dir. Hint: If you're working in Eclipse, you can create an environment variable inside eclipse");
        }

        prefs = PrefsHelper.getConfig(TEST_APP_NAME);
    }

    @After
    public void tearDown() {
        File f = new File(filePath);
        assertTrue(f.delete());

        File d = new File(dirPath);
        assertTrue(d.delete());
    }

    /*
     * Test getting preferences from classpath
     * 
     * I removed ability to load 2 properties files at the same time because
     * CombinedConfiguration didn't seem to be working.
     */
    // @Test
    // public void testClassPathProps() {
    // assertEquals("yep only in classpath",
    // prefs.getString("only.defined.in.classpath"));
    // }

    /*
     * OS ENV must be set for this test to run successfully. This will create a
     * file named app1.properties under "OS_ENV/app1" and then try to find a
     * property based on that.
     */
    @Test
    public void testExternalProps() {
        assertEquals("External Properties is working",
                prefs.getString("only.defined.in.external"));
        assertEquals("this is the external value",
                prefs.getString("defined.in.both.places"));
    }

    /*
     * Test to ensure that we can change the properties file without restarting
     * thea app
     */
    @Test
    public void testReloadValues() {
        try {
            BufferedReader b = new BufferedReader(new FileReader(filePath));
            String line = "";
            String old = "";
            while ((line = b.readLine()) != null) {
                old += line + "\r\n";
            }
            b.close();

            String newtext = old.replaceAll("External Properties is working",
                    "External Properties is changed!");
            FileWriter w = new FileWriter(filePath);
            w.write(newtext);
            w.close();

            /*
             * commons-configuration checks whether files change every 5
             * seconds, so, here we wait for 5 seconds, and then update the
             * timestamp
             */
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new AssertionError();
            }

            File f = new File(filePath);
            f.setLastModified(Calendar.getInstance().getTimeInMillis());

        } catch (Exception e) {
            throw new AssertionError(e);
        }

        assertEquals("External Properties is changed!",
                prefs.getString("only.defined.in.external"));
        assertEquals("this is the external value",
                prefs.getString("defined.in.both.places"));
    }

}
