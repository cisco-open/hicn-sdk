## Android SDK ##

This is the hICN Distillery software distribution for Android. It is in charge of pulling
together all the necessary modules to build a full hICN software suite for arm64 and x86_64 Android arch.

## Dependencies ##

Install dependencies

If Ubuntu:

```
sudo apt-get install git wget python curl automake libconf libtool openjdk-8-jdk
```

If Mac Os X

```
brew install git wget automake libconf libtool gnu-sed coreutils
```


## Quick Start ##

Clone this distro

```
git clone https://github.com/icn-team/android-sdk.git
cd android-sdk
```

Compile everything (dependencies and hICN modules)

```
make update
export ANDROID_ARCH="arm64"
make all
export ANDROID_ARCH="x86_64"
make all
```

Compile viper dependencies

```
echo "export QT_CI_LOGIN=<qt username>"
echo "export QT_CI_PASSWORD=<qt password>"

export ANDROID_ARCH="arm64"
make build-qtdep
export ANDROID_ARCH="x86_64"
make build-qtdep
```

The hICN Distillery software will be installed in android-sdk/usr_aarch64 and android-sdk/usr_x86_64


To compile Hybrid ICN Network Service for android app

```
make android_hicnforwarder GITHUB_USER=<github user> GITHUB_TOKEN=<github token>
```

To install the application run

```
# Optionally, uninstall previous version (to avoid signature mismatch issues)
adb uninstall com.cisco.hicn.forwarder

adb install -r ./HicnForwarderAndroid/app/build/outputs/apk/release/HicnForwarderAndroid.apk
```

To compile Hybrid ICN SpeedTest & Test android app

```
make android_hicntools GITHUB_USER=<github user> GITHUB_TOKEN=<github token>
```

To install the application run

```
adb install -r ./app/build/outputs/apk/release/hICN_Tools.apk
```

To compile Viper ABR video player for android app 

```
make android_viper
```

To install the application run

```
adb install -r build_aarch64/viper/hicn-viper-arm64_v8a/build/outputs/apk/hicn-viper-arm64_v8a-release-signed.apk
```

To compile Common aar module

```
make android_hicntools
```

To compile hicn-light forwarder aar module

```
make android_forwarderlibraryaar GITHUB_USER=<github user> GITHUB_TOKEN=<github token>
```

To compile face manager aar module

```
make android_facemgrlibraryaar GITHUB_USER=<github user> GITHUB_TOKEN=<github token>
```



## Platforms ##

- Android



## Getting Started ##

To get simple help run `make`. This will give you a list of possible targets to
execute. You will basically want to download all the sources and compile.

Here's a short summary:
    
- `make help`                                        - This help message
- `make update`                                        - Update hicn to the right commit
- `make all`                                        - Download sdk, ndk and dependencies, configure, compile and install all software in DISTILLERY_INSTALL_DIR
- `make all-withqt`                                        - Download sdk, ndk, qt and dependencies, configure, compile and install all software in DISTILLERY_INSTALL_DIR
- `make init_sdk`                                        - Download sdk and ndk
- `make init_qt`                                        - Download the qt framework
- `make init_depend`                                        - Download sdk, ndk and dependencies, compile and install all dependencies in DISTILLERY_INSTALL
- `make init_depqt`                                        - Download qt and the qt/viper dependencies, compile and install them in DISTILLERY_INSTALL
- `make install-all`                                        - Configure, compile and install all software in DISTILLERY_INSTALL_DIR
- `make curl-clean`                                        - Clean curl files and libs
- `make openssl-clean`                                        - Clean opennssl files and libs
- `make asio-clean`                                        - Clean asio files
- `make event-clean`                                        - Clean libevent files and libs
- `make ffmpeg-clean`                                        - Clean ffmpeg files and libs
- `make libconfig-clean`                                        - Clean libconfig files and libs
- `make xml2-clean`                                        - Clean libxml2 files and libs
- `make libdash-clean`                                        - Clean libdash files and libs
- `make viper-clean`                                        - Clean viper files
- `make dependencies-clean`                                        - Clean all dependencies files and libs
- `make sdk-clean`                                        - Clean sdk files
- `make ndk-clean`                                        - Clean ndk files
- `make androidsdk-clean`                                        - Clean sdk, ndk and cmake files
- `make libparc-clean`                                        - Clean libparc files and libs
- `make hicn-clean`                                        - Clean hicn files and libs
- `make all-clean`                                        - Clean    all files and libs
- `make android_hicnforwarder`                                        - Build HicnForwader apk for android
- `make android_hicnforwarder_debug`                                        - Build HicnForwader apk for android in debug mode
- `make android_hicntools`                                        - Build HicnTools apk for android
- `make android_hicntools_debug`                                        - Build HicnTools apk for android in debug mode
- `make android_viper`                                        - Build Viper apk for android apk in debug mode (only arm64)
- `make android_viper_debug`                                        - Build Viper apk for android apk (only arm64)"
- `make android_commonaar`		- Build common aar module"
- `make android_commonaar_debug`	- Build common aar module for android in debug mode"
- `make android_forwarderlibraryaar GITHUB_USER=<github user> GITHUB_TOKEN=<github token>`	- Build hicn-light forwarder aar module
- `make android_forwarderlibraryaar_debug	GITHUB_USER=<github user> GITHUB_TOKEN=<github token>` - Build hicn-light forwarder aar module in debug mode
- `make android_facemgrlibraryaar GITHUB_USER=<github user> GITHUB_TOKEN=<github token>`	- Build face manager aar module
- `make android_facemgrlibraryaar_debug	GITHUB_USER=<github user> GITHUB_TOKEN=<github token> - Build face manager aar module in debug mode
- `make version`                                        - Print the version of installed modules


## Use hicn aar modules in custom application ##

In app/build.gradle add

```
repositories {
    maven {
        name = 'GitHubPackagesPublic'
        url = uri('https://maven.pkg.github.com/icn-team/android-sdk')
        credentials {
            username = <github user>
            password = <github token>
        }
    }
}

dependencies {
    implementation 'com.cisco.hicn:common:+'
    implementation 'com.cisco.hicn:hicnforwarder:+'
    implementation 'com.cisco.hicn:facemgr:+'
}
```





## Configuration ##

Distillery can be configured in multiple ways.  Please check the config directory (specifically `config/config.mk`) for more information.

## Command line tools ##

The hICN distillery makes also available some command line tools which can be very helpful for
debugging and check the status of the active components.

After you compile the hICN software suite:

```bash
bash ./compileAll.sh
```

Copy the output folder (`usr_aarch64` or `usr_x86_64`, depending on the architecture) in the phone using adb:

```bash
adb push ./usr_aarch64 /data/local/tmp/
```

Access the phone with adb shell:

```bash
adb shell
```

Go inside the copied folder and run hicn-light-control:

```bash
cd /data/local/tmp/usr_aarch64/bin
chmod +x hicn-light-control
./hicn-light-control
```

This command line tools allows to check the current status of the local forwarder.
In particular, you can check the current list of connections running this command:

```bash
./hicn-light-control list connections
```

And you can check the current FIB with this command:

```bash
./hicn-light-control list routes
```
