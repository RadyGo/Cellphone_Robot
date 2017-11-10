# Cellphone_robot

Welcome to the Cellphone_Robot project! Through this project we are aiming to expand on Android based robotics. myRobot, the app within this repository, was created to control a small ROS controlled car-robot. The project showcases how an Android phone can significantly enhance a ROS-based robot through leveraging the many APIs out there. This project utlizes multiple libraries as well as APIs. In this user guide we will walk you through setting up the project for yourself and setting up the necessary access keys to use the app in tandem with a ROS project. That being said, all the libraries and APIs can be replaced or expanded upon so do not feel limited to our implementation of the repo. 

This repo allow the robot to: access a Natural Language Processor, translate text, send SMS, send Slack messages, send GroupMe messages, post to Twitter, perform basic image processing, and communicate back and forth with a ROS network. In our hardware setup, we had the ROS network running on a Raspberry Pi 3 mounted on the RC robot itself. We used a portable rechargable battery to make this happen. The phone is also mounted on the robot using a cellphone holder. Have fun experimenting!

## What does the app do?
The app is fundamentally acting as an extension to the robot, regardless of what the robot is or what it is capable of. The app acts as a higher level method of controlling the robot, while leveraging its access to the web and the many tools out there for Android phones. Once the robot is connected to the phone, the phone knows what the robot is capable of doing. The user can then communicate with the phone through texting or Instant Messaging over NLP to command the robot. This project is essentially a template for people to get creative with. If you have a ROS based robot and an Android phone, you can do all sorts of cool stuff.

# ROS Setup
This user guide focuses mainly on setting up the Android app; however, to communicate with the ROS network a few steps need to be taken. This [repo](https://github.com/wang3303/ros_cellphonerobot) contains the ROS project we created to test the app with. To use the app you do not need to clone the whole project. There is only one script that your ROS project needs to have. [That](https://github.com/wang3303/ros_cellphonerobot) user guide will show you how to set up the script for your own uses, or you can just clone the whole project.

## Script
//TODO: Michael, you need to put instructions here. Also include the script in the current repo.

## Connection
To connect the Android to ROS, you need to make sure the `ROS_MASTER_URI` is correctly referenced and that depends on whether the connection is over WIFI or USB.

### WIFI
Ensure the ROS host machine, in our case the Rhaspberry PI, and the Android phone are connected to the same WIFI network. 

On the ROS host, copy the `ROS_MASTER_URI`. Then in terminal, type the following commands:

//TODO: ADD COMMANDS HERE

```terminal
$ cd ..
$ echo "export ROS_MASTER_URI=http://localhost:11311" >> .bashrc
$ echo "source
```

On the Android side, go to `strings.xml` located under [`/app/res/values`](/app/res/values) and tyoe in the `ROS_MASTER_URI` in the corresponding location.

```xml
 <string name="rosIP">http://TYPE_IP_HERE:11311/</string>
 
```
### USB
Connect the phone to the ROS host. Turn off WIFI connection on the ROS host device. On the Android, go to **Settings > More > Tethering & portable hotspost** and turn on **USB tethering**.

On the ROS host device, go to terminal and type `$ config`
//TODO: Find out the ethernet IP
//TODO: Input in .bashrc
//TODO: Update strings.xml  

# Android Setup
## Libraries
### OpenCV
OpenCV provides some tutorials and documentation through [here](https://opencv.org/platforms/android/). This library is processor dependent, so you need to find out your phone's ARM processor version and allocate the correct file to the jniLibs directory. 

First, make sure you have the [Android Debug Bridge (adb)](https://developer.android.com/studio/command-line/adb.html) installed.

After the installation is complete, run this: `adb shell getprop ro.product.cpu.abilist`. The output will tell you the phone's ARM processor version. For example, when I run the above command on a [Huawei Honor 5x](http://www.hihonor.com/global/products/mobile-phones/honor5x/index.html), the output is `arm64-v8a,armeabi-v7a,armeabi`.

I can pick any processor of the three to allocate to the OpenCV library. For the guide's purpose, let's choose `arm64-v8a`. 

Next, go to [`processor_libs`](/processor_libs) and copy the corresponding version directory. In this case, `arm64-v8a`. Afterwards, go to [`/app/rc/main/niLibs`](/app/rc/main/niLibs) and paste `arm64-v8a`. 

Now that you're done allocating the correct processor version file to OpenCV, delete `processor_libs`. You no longer need it and it is a relatively large folder.

### ailib
// TODO: User guide setup by Michael

## APIs
The app includes many APIs, all of which need access keys. Below are the respective links for obtaining them. All you need to do to have the APIs work is paste the keys in the corresponding `string.xml` allocation.

### Twitter
This [link](https://apps.twitter.com) will take you to registering your app for access keys. Once the registration process is done, go to `strings.xml` located under [`/app/res/values`](/app/res/values). Paste your respective access keys in the corresponding locations.

```xml
<string name="twitterSetOAuthConsumerKey">HERE</string>
<string name="twitterSetOAuthConsumerSecret">HERE</string>
<string name="twitterSetOAuthAccessToken">HERE</string>
<string name="twitterSetOAuthAccessTokenSecret">HERE</string>
```

### Slack
// TODO: User guide setup by Michael

### GroupMe
// TODO: User guide setup by Michael



This project is still in early development, further updates and robots will be coming soon. In the meantime please feel free to delve into our extensive [wiki](https://github.com/AGKhalil/Cellphone_Robot/wiki) or the first [research paper](https://github.com/AGKhalil/Cellphone_Robot/blob/master/Cell%20Phone%20Robot%20Paper.pdf) written surrounding this project and Lily, our first robot.

Here's a picture of Lily!
<p align="center">
<img src="https://github.com/AGKhalil/Cellphone_Robot/blob/master/wiki_images/Lily/FrontShot.png" width="400">
</p>
<p align="center">
Front view of Lily
</p>