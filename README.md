# SLAMBench for Android  [![Build Status](https://travis-ci.org/bbodin/slambench-android.svg?branch=master)](https://travis-ci.org/bbodin/slambench-android)

This repository contains the Android version of SLAMBench. 
SLAMBench for Android uses the KinectFusion algorithm to evaluate the CPU (and the GPU) performance of mobile phones.

### Related Papers

B. Bodin, L. Nardi, M. Z. Zia, H. Wagstaff, G. S. Shenoy, M. Emani, J. Mawer, C. Kotselidis, A. Nisbet, M. Lujan, B. Franke, Paul H. J. Kelly, M. O’Boyle. **Integrating algorithmic parameters into benchmarking and  design space exploration in dense 3D scene understanding.** In Intl. Conf. on Parallel Architectures and Compilation Techniques (PACT 2016), Haifa, September 2016.

L. Nardi, B. Bodin, M. Z. Zia, J. Mawer, A. Nisbet, P. H. J. Kelly, A. J. Davison, M. Luján, M. F. P. O’Boyle, G. Riley, N. Topham, and S. Furber. **Introducing SLAMBench, a performance and accuracy benchmarking methodology for SLAM.** In IEEE Intl. Conf. on Robotics and Automation (ICRA 2015), Seattle, Washington USA, May 2015.

### More informations
* Android Application: https://play.google.com/store/apps/details?id=project.pamela.slambench
* SLAMBench website: http://apt.cs.manchester.ac.uk/projects/PAMELA/tools/SLAMBench/
* SLAMBench repository: https://github.com/pamela-project/slambench
* KinectFusion: https://msdn.microsoft.com/en-us/library/dn188670.aspx
* KFusion: https://github.com/GerhardR/kfusion

## About the benchmark algorithm

KinectFusion is a Simultaneous Localization And Mapping algorithm (SLAM) and it uses an RGB-D sensor (like the XBox Kinect).

SLAM algorithms are used:

* to localize a camera in the scene it records, and 
* to reconstruct the recorded scene.

RGB-D sensors are :

* a standard camera (RGB), and
* a depth camera (D) to get the distance between the camera and the visible points of the scene.

We are using the SLAMBench implementation (C++,OpenMP,OpenCL) of this algorithm, which itself is inspired from the KFusion implementation (CUDA only).
SLAMBench is a framework which includes speed, accuracy and power measurement. 

## About the study

Many vendors include OpenCL in their mobile devices to use the GPUs for computation, and this provides opportunities for real-time vision applications.
Vendors also started to integrate RGB-D sensor in their devices, we investigate if KinectFusion is a suitable solution for mobile devices with or without OpenCL.

## About the configurations

KinectFusion can be parametrized, but its default configuration is too slow to run on any mobile phones (even the most powerful one).
We are using custom configurations (called fast1 to fast4) which make possible to run KinectFusion on these mobile phones.

# How to compile it

Well, there is no standard procedure yet, but here is a way...

## Quick compilation

Just a ```make``` should work, but please **read the preparation before.**

## Preparation

I prepared it on a clean Fedora 24 Workstation, and this is what I did first :

* Install** Java 1.8**, **Python 2.7**, **GCC**
```
  dnf install java-1.8.0-openjdk java-1.8.0-openjdk-devel 
  dnf install python
  dnf install gcc gcc-c++
```
* If you are running on a 64bit machine you will need to install 32bits dependencies.
```
  dnf install libstdc++-devel.i686 zlib.i686
```
* Install **android-studio** -- optional -- ([https://developer.android.com/studio/index.html](https://developer.android.com/studio/index.html))
```
  wget https://dl.google.com/dl/android/studio/ide-zips/2.1.2.0/android-studio-ide-143.2915827-linux.zip
  unzip android-studio-ide-143.2915827-linux.zip
  mkdir -p ~/.local/Android/
  mv android-studio ~/.local/Android/android-studio
  ~/.local/Android/android-studio/bin/studio.sh
```
* Install **Android SDK 23** ([https://developer.android.com/studio/index.html](https://developer.android.com/studio/index.html))
 this includes then Android SDK Platform-tools , Android SDK Tools , SDK Platform Android 5.1.1 API 23, Android SDK Build-tools 23.0.2, Google APIs Android API 23, Android Support Repository
```
  wget https://dl.google.com/android/android-sdk_r24.4.1-linux.tgz
  tar xzf android-sdk_r24.4.1-linux.tgz
  mv android-sdk-linux ~/.local/Android/Sdk
  ~/.local/Android/Sdk/tools/android list sdk --all
  ~/.local/Android/Sdk/tools/android update sdk -u -a -t "platform-tools,tools,android-23,build-tools-23.0.2,extra-android-m2repository,addon-google_apis-google-23"   
```
or just type ```make sdk```.
* Install **Android NDK r12b** ([https://developer.android.com/ndk/downloads/index.html](https://developer.android.com/ndk/downloads/index.html))

```
  wget http://dl.google.com/android/repository/android-ndk-r12b-linux-x86_64.zip
  unzip android-ndk-r12b-linux-x86_64.zip
  mv android-ndk-r12b ~/.local/Android/android-ndk-r12b
```
or just type ```make ndk```.

Then you need to fix the absolute paths in ./local.properties:
```
echo "sdk.dir=/home/`whoami`/.local/Android/Sdk" > ./local.properties
echo "ndk.dir=/home/`whoami`/.local/Android/android-ndk-r12b" >> ./local.properties
```

## Compilation

Just a ```make``` should work.

## Installation

Once the apk is built, you can install it on a mobile phone using the following command :
```
adb install ./app/build/outputs/apk/app-debug.apk
```


## Know errors

- The APK compilation failed with an error like :
```
> Failed to apply plugin [id 'com.android.application']
   > Minimum supported Gradle version is ???? If using the gradle wrapper, try editing the distributionUrl in gradle-wrapper.properties to gradle-2.14.1-all.zip

```
If this error occurs the best is to do as recommanded, just update the gradle-wrapper.properties file.

- Feel free to fill this section.
- ...