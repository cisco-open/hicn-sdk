### use this source as library

Download this repo in the root of android-sdk

Create a symbolic link of hproxylibrarySrc and semtun-manager
ln -s ../hproxy-aar/hproxyLibrarySrc HicnForwarderAndroid/
ln -s ../hproxy-aar/semtun-manager HicnForwarderAndroid/



Open HicnForwarderAndroid project in android studio


In HicnForwarderAndroid project
open the file settings.gradle
comment the line include ':hproxyLibrary'
add the lines
include ':hproxyLibrarySrc'
include ':semtun-manager'

open the file app/build.gradle
modify the line
implementation project(":hproxyLibrary")
with the line
implementation project(":hproxyLibrarySrc")
 and add the line
implementation project(":semtun-manager")
