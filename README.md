# The Problem

Java projects usually expect to find properties files on the class path (usually inside jar or war files). But there are times when it's convenient to be able to update values inside a properties file without having to unpack the war or jar. It would be nice if the properties files could be stored outside the jar or war. But then, the problem is: how can the java app find an external properties file without hard coding a file system path?

# Prefomatic Solution

Prefomatic uses the following algorithm: 

## Step 1: Determine Operating System Environment Variable Name

* If a "prefomatic.properties" file exists inside the application's classpath, then find a key named `prefomatic.env_var_name` to determine the name of the Operating System Environment Variable to use. 
* If "prefomatic.properties" file doesn't exist (or no key named `prefomatic.env_var_name` can be found), then default to use OS Env Variable name of "CUSTOM_JAVA". 

## Step 2: Determine external properties directory path

* Next, if the Operating System Environment Variable (found in Step 1) exists, then get the value. The value is expected to be a directory path.
* The Name of the application is appended to the directory path. 
* Check if directory path + app name exists. If it does, this is the external location to look for properties files. 
* If the Operating System Env Variable can't be found, or the directory doesn't exist, then prefomatic just falls back to checking inside the classpath for properties files. 

## Step 3: Get key/value pairs from property files

* Once you have a handle on the Apache Commons Configuration object passed back from prefomatic, you can use it to look up key/value pairs from property files. 
* The external properties file is checked first. If the key is found, a value is returned.
* If the key is not found in the external properties file, then the properties file inside the classpath is checked. If a key is found, the value is returned. 
* If the key doesn't exist in either the external nor the classpath properties file, then a default value is passed back if supplied (using Apache Commons Configuration mechanism). 

# Usage

## Create prefomatic.properties (and make sure its on the app classpath)

    $> echo prefomatic.env_var_name=MY_JAVA_PREFS > prefomatic.properties
    

## Create Operating System Environment Variable with same name

    $> export MY_JAVA_PREFS=/home/upgradingdave/java/config

## Create your custom app properties file

    $> mkdir /home/upgradingdave/java/config/example
    $> cat name=Dave > /home/upgradingdave/java/config/example/example.properties

## Use it in your code

    import org.apache.commons.configuration.Configuration;
    import com.upgradingdave.prefs.PrefsHelper;

    public class Example {
           //AppName must match directory name created above
           String AppName = "example";
           
           //This only needs to be called once per application
           Configuration prefs = PrefsHelper.getConfig(AppName);
           
           //read name from external properties file
           String name = prefs.getString("name", "DefaultName");
    }

You only need to call `PrefsHelper.getConfig(AppName);` once. After that, simply use `PrefsHelper.getConfig();` everywhere else in your app. 

## Use it in a servlet

    <servlet>
      <servlet-name>PropertyServlet</servlet-name>
      <servlet-class>com.upgradingdave.prefs.InitServlet</servlet-class>
      <init-param>
        <!-- This tells prefomatic to look for $OS_ENV/example/example.properties -->
        <!-- Then, you can simply use PrefsHelper.getConfig(); in servlet code -->
        <param-name>app-name</param-name>
        <param-value>example</param-value>
      </init-param>
      <load-on-startup>1</load-on-startup>
    </servlet>

## Use it with IoC

Prefomatic plays nicely with frameworks like Spring too!



