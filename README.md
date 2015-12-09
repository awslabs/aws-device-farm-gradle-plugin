AWS Device Farm Gradle Plugin
------------------------------

AWS Device Farm integration with Android Gradle Build system

This plugin provides [AWS Device Farm] (http:// aws.amazon.com/device-farm) functionality from your Android gradle environment, allowing you to kick off tests on real Android phones and tablets hosted in the AWS Cloud.

For more information see [AWS Device Farm Developer Guide] (http:// docs.aws.amazon.com/devicefarm/latest/developerguide/welcome.html)


Usage
=====

## Building the plugin:

Building the plugin is optional.  The plugin is published through Maven Central.  If you wish to allow gradle to download the plugin directly skip this section and jump to [Using the plugin](#using-the-plugin)  

1. Clone the GitHub repository.
2. Build the plugin using ```gradle install```.
3. The plugin will be installed to your local maven repository.

## Using the plugin:
1. Add the plugin artifact to your dependency list in build.gradle.

```
    buildscript {
    
        repositories {        
            mavenLocal()            
            mavenCentral()            
        }
        
        dependencies {        
            classpath 'com.android.tools.build:gradle:1.3.0'           
            classpath 'com.amazonaws:aws-devicefarm-gradle-plugin:1.0'            
        }        
    }
```

2. Configure the plugin in your build.gradle file. See below for test specific configuration.

```
    apply plugin: 'devicefarm'
    
    devicefarm {
    
        projectName "My Project" // required: Must already exists.
        
        devicePool "My Device Pool Name" // optional: Defaults to "Top Devices"
        
        useUnmeteredDevices() // optional if you wish to use your un-metered devices
        
    
        authentication {        
            accessKey "aws-iam-user-accesskey"            
            secretKey "aws-iam-user-secretkey"         
                   
            // or
            
            roleArn "My role arn" // Optional, if role arn is specified, it will be used.  
                                  // Otherwise use access and secret keys
        }
    
        // optional block, radios default to 'on' state, all parameters optional
        devicestate {
 
            extraDataZipFile file("relative/path/to/zip") // default null
            auxiliaryApps [file("path1"), file("path2")] // default empty list
            wifi “on”
            bluetooth ”off”
            gps ”off”
            nfc ”on”
            latitude 47.6204 // default
            longitude -122.3491 // default
        }
     
    
        // Configure test type, if none default to instrumentation
        // Fuzz         
        // fuzz { }        
    
        // Instrumentation      
        // See AWS Developer docs for filter (optional)
        // instrumentation { filter "my-filter" }
            
        // Calabash        
        calabash { 
           
           tests file("path-to-features.zip")
           
        }
        
    }
```

3. Run your Device Farm test with the devicefarmUpload task. ( ```gradle devicefarmUpload```)
4. The build output will print out a link to the AWS Device Farm console where you can monitor your test execution.

## Generating a proper IAM user:

1. Log into your AWS web console UI.
2. Click "Identity & Access Management".
3. On the left-hand side of the screen, click "Users".
4. Click "Create New Users".
5. Enter a user name of your choice.
6. Leave the "Generate an access key for each user" checkbox checked.
7. Click "Create".
8. View or optionally download the User security credentials that were created; you will them them later.
9. Click "Close" to return to the IAM screen.
10. Click your user name in the list.
11. Under the Inline Policies header, click the "click here" link to create a new inline policy.
12. Select the "Custom Policy" radio button.
13. Click "Select".
14. Give your policy a name under "Policy Name".
15. Copy/paste the following policy into "Policy Document"

```
    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Sid": "DeviceFarmAll",
                "Effect": "Allow",
                "Action": [ "devicefarm:*" ],
                "Resource": [ "*" ]
            }
        ]
    }
```
16. Click "Apply Policy".

Test Type configuration
=======================


Appium
------
* [Appium JUnit] (http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-android-appium-java-junit.html)
* [Appium TestNG] (http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-android-appium-java-testng.html)

Device Farm provides support for Appium Java TestNG and JUnit for Android. 

You can choose to ```useTestNG()``` or ```useJUnit()```
JUnit is the default and does not need to be explicitly specified.
```
    appium {
        tests file("path to zip file") // required
        useTestNG() // or useJUnit()
    }
```

[Built-in: Explorer] (http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-built-in-explorer.html)
-------------

Use AWS Device Farm's app explorer to test user flows through your app without writing custom test scripts.
A username and password may be specified in the event your app requires account log-in.
```
    appexplorer {
        username "my-username"
        password "my-password"
    }
```
[Built-in: Fuzz] (http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-built-in-fuzz.html)
------

Device Farm provides a built-in fuzz test type. 

The built-in fuzz test randomly sends user interface events to devices and then reports results.

```
    fuzz {
    
       eventThrottle 50 // optional default
       eventCount 6000  // optional default
       randomizerSeed 1234 // optional default blank
    
     }
```

[Calabash] (http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-android-calabash.html)
----------

Device Farm provides support for Calabash for Android.
* [Prepare Your Android Calabash Tests] (http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-android-calabash.html#test-types-android-calabash-prepare)

```
    calabash {
        tests file("path to zip file") // required
        tags "my tags" // optional calabash tags
        profile "my profile" // optional calabash profile
    }
```

[Instrumentation] (http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-android-instrumentation.html)
-----------------

Device Farm provides support for Instrumentation (JUnit, Espresso, Robotium, or any Instrumentation-based tests) for Android.

When running an instrumentation test in gradle the apk generated from your androidTest directory will be used as the source of your tests.

```
    instrumentation { 
    
        filter "test filter per developer docs" // optional

    }
```

[UI Automator] (http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-android-uiautomator.html)
-------------

Upload your app as well as your UI Automator based tests packaged in a jar file
```
    uiautomator {
        tests file("path to uiautomator jar file") // required
        filter "test filter per developer docs" // optional

    }
```




Adding New Frameworks
=====================

As Device Farm supports additional test types the plugin must be extended to enable them into the DSL.


1. Define what the DSL should look like.
2. Create a class in the com.amazonaws.devicefarm.extension package to support the DSL, it must extend the ConfiguredTest abstract class.
  1. If the test type requires a test artifact package to be uploaded in addition to the app then make sure the new class extends the TestPackageProvider trait.
  2. If the test type supports the 'filter' parameter extend the HasFilter trait.
3. Modify DeviceFarmExtension to add a method for the new test type, like so:
```
    void mynewtesttype(final Closure closure) {
        NewTestType newTest = new NewTestType()
        project.configure newTest, closure
        test = newTest
    }        
```
* This will enable the configuration of the new test type within the devicefarm plugin.


Dependencies
============

Runtime
-------

* AWS SDK 1.10.15 or later.
* Android tools builder test api 0.5.2
* Apache Commons Lang3 3.3.4

For Unit tests
--------------
 
* Testng 6.8.8
* Jmockit 1.19
* Android gradle tools 1.3.0 
