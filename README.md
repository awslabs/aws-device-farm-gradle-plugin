AWS Device Farm Gradle Plugin
------------------------------

AWS Device Farm integration with the Android Gradle Build system

This plugin provides [AWS Device Farm](http://aws.amazon.com/device-farm) functionality from your Android gradle environment, allowing you to kick off tests on real Android phones and tablets hosted in the AWS Cloud. 

For more information see the [AWS Device Farm Developer Guide](http://docs.aws.amazon.com/devicefarm/latest/developerguide/welcome.html). 

Usage
=====

1. Add the Device Farm plugin artifact. Paste the following into your top-level `build.gradle`. 

```gradle
buildscript {

    repositories {        
        mavenCentral()            
    }
    
    dependencies {        
        classpath 'com.amazonaws:aws-devicefarm-gradle-plugin:1.3'
    }        
}
```

2. Configure the Device Farm plugin in your module’s `build.gradle` file. This is usually `app/build.gradle`.  

### Minimal configuration

This will do the following: 

* Start a Run under the Project "My Project"
* Use the "Top Devices" curated Device Pool
* Run your instrumentation tests under `androidTest`

```gradle
apply plugin: 'devicefarm'

devicefarm {

    // Required. The Project must already exist. You can create a project in the AWS console.
    projectName "My Project" 
    
    // Required. You must specify either accessKey and secretKey OR roleArn. roleArn takes precedence. 
    authentication {
        accessKey "aws-iam-user-accesskey"
        secretKey "aws-iam-user-secretkey"
        
        // OR
        
        roleArn "My role arn"
    }
}
```

### Advanced configuration

```gradle
apply plugin: 'devicefarm'

devicefarm {

    // Required. The Project must already exist. You can create a project in the AWS console.
    projectName "My Project" // required: Must already exist.
    
    // Optional. Defaults to "Top Devices"
    devicePool "My Device Pool Name"
    
    // Optional. Default is 150 minutes
    executionTimeoutMinutes 150
    
    // Optional. Set to "off" if you want to disable device video recording during a run. Default is "on"
    videoRecording "on"
    
    // Optional. Set to "off" if you want to disable device performance monitoring during a run. Default is "on"
    performanceMonitoring "on"
    
    // Optional. Add this if you have a subscription and want to use your unmetered slots
    useUnmeteredDevices()
    
    // Required. You must specify either accessKey and secretKey OR roleArn. roleArn takes precedence. 
    authentication {
        accessKey "aws-iam-user-accesskey"
        secretKey "aws-iam-user-secretkey"
        
        // OR
        
        roleArn "My role arn"
    }

    // Optional block. Radios default to 'on' state, all parameters are optional
    devicestate {
        extraDataZipFile file("path/to/zip") // or ‘null’ if you have no extra data. Default is null.
        auxiliaryApps files(file("path/to/app"), file("path/to/app2")) // or ‘files()’ if you have no auxiliary apps. Default is an empty list.
        wifi "on"
        bluetooth "off"
        gps "off"
        nfc "on"
        latitude 47.6204 // default
        longitude -122.3491 // default
    }
 
    // Optional. Set the test type. Default is instrumentation. 
    // You can only set one test type. 
    // See "Test Type configuration" below for configuration details

    // Fuzz
    fuzz {
    }

    // Instrumentation
    instrumentation {
        // Optional. See the AWS Developer docs for filter rules
        filter "my-filter"
    }

    // Calabash        
    calabash {
       tests file("path-to-features.zip")
    }
}
```

3. Run your configured test on Device Farm with the `devicefarmUpload` task. ( `./gradlew devicefarmUpload`)
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

```json
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

* [Appium JUnit](http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-android-appium-java-junit.html)
* [Appium TestNG](http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-android-appium-java-testng.html)

Device Farm provides support for Appium Java TestNG and JUnit for Android. 

You can choose to `useTestNG()` or `useJUnit()`. 

JUnit is the default and does not need to be explicitly specified.

```gradle
appium {
    tests file("path to zip file") // Required
    useTestNG() // or useJUnit()
}
```

[Built-in: Explorer](http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-built-in-explorer.html)
--------------------

Use AWS Device Farm's app explorer to test user flows through your app without writing custom test scripts.
A username and password may be specified in the event your app requires account log-in.

```gradle
appexplorer {
    username "my-username"
    password "my-password"
}
```
[Built-in: Fuzz](http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-built-in-fuzz.html)
----------------

Device Farm provides a built-in fuzz test type. 

The built-in fuzz test randomly sends user interface events to devices and then reports results.

```gradle
fuzz {
    eventThrottle 50 // Optional. Default is 50
    eventCount 6000  // Optional. Default is 6000
    randomizerSeed 1234 // Optional. Default is blank
}
```

[Calabash](http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-android-calabash.html)
----------

Device Farm provides support for Calabash for Android.
* [Prepare Your Android Calabash Tests](http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-android-calabash.html#test-types-android-calabash-prepare)

```gradle
calabash {
    tests file("path to zip file") // Required
    tags "my tags" // Optional. Calabash tags
    profile "my profile" // Optional. Calabash profile
}
```

[Instrumentation](http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-android-instrumentation.html)
-----------------

Device Farm provides support for Instrumentation (JUnit, Espresso, Robotium, or any Instrumentation-based tests) for Android.

When running an instrumentation test in gradle the apk generated from your androidTest directory will be used as the source of your tests.

```gradle
instrumentation { 
    filter "test filter per developer docs" // Optional
}
```

[UI Automator](http://docs.aws.amazon.com/devicefarm/latest/developerguide/test-types-android-uiautomator.html)
--------------

Upload your app as well as your UI Automator based tests packaged in a jar file
```gradle
uiautomator {
    tests file("path to uiautomator jar file") // Required
    filter "test filter per developer docs" // Optional
}
```

Building the plugin
===================

Building the plugin is optional.  The plugin is published through Maven Central. 

1. Clone the GitHub repository.
2. Clean, assemble and test the plugin using `./gradlew clean build`
3. Install the plugin into your local maven directory using `./gradlew install`.
4. The plugin will be installed to your local maven repository.

Adding New Frameworks
---------------------

As Device Farm supports additional test types the plugin must be extended to enable them into the DSL.


1. Define what the DSL should look like.
2. Create a class in the com.amazonaws.devicefarm.extension package to support the DSL, it must extend the ConfiguredTest abstract class.
  1. If the test type requires a test artifact package to be uploaded in addition to the app then make sure the new class extends the TestPackageProvider trait.
  2. If the test type supports the 'filter' parameter extend the HasFilter trait.
3. Modify DeviceFarmExtension to add a method for the new test type, like so:
```groovy
void mynewtesttype(final Closure closure) {
    NewTestType newTest = new NewTestType()
    project.configure newTest, closure
    test = newTest
}        
```
* This will enable the configuration of the new test type within the devicefarm plugin.
