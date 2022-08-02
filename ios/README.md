## iOS SDK ##
This is the hICN Distillery software distribution for iOS. It is in charge of
pulling together all the necessary modules to build a full hICN software suite
for arm64, arm64e and x86-64 arch for iPhoneOS and iOS.

## Quick Start ##

Clone this distro

```
cd ios-sdk
```

Compile everything (dependencies and hICN modules)

```
make update
make all
```

Compile everything with Qt (dependencies, hICN modules and Viper dependencies)

```
make update
make all_qt
```
