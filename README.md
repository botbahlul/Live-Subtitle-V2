# Live-Subtitle-V2
ANDROID APP that will recognize LIVE AUDIO/VIDEO STREAMING then TRANSLATE (using online google-translate-api) and display it as SUBTITLES

This app has PROS & CONS compared to other Speech Recognition APIs like VOSK, IBM WATSON, and PREMIUM GOOGPLE SPEECH API

PROS:
It's FREE and supports all languages supported by Google with very good accuracy

CONS:
It currently can only listen if you PLAY YOUTUBE STREAM (NOT ALL STREAMS!) via ANDROID VLC PLAYER

VLC UPDATE NOTES : IF YOUR VLC CANNOT PLAY DIRECT YOUTUBE LINK URL ANYMORE, YOU NEED TO UPDATE youtube.lua SCRIPT INSIDE VLC

There are 2 ways to update youtube.lua script :

1. FOR ROOTED ANDROID DEVICE :

Replace youtube.lua in android storage with ES FILE MANAGER or FX FILE MANAGER :

```
/data/data/org.videolan.vlc/app_vlc/.share/lua/playlist/youtube.lua
```

2. FOR NON ROOTED ANDROID DEVICE :

Use APKTOOL https://ibotpeaches.github.io/Apktool/install/ to modify VLC.APK and APK EDITOR STUDIO https://qwertycube.com/apk-editor-studio/download/ to sign it

- download apktool.jar and place it into a folder that has been already added to system environment path (e.g. in C:\Windows\system32)

- create apktool.bat (or executable script if you're linux user) just like described in https://ibotpeaches.github.io/Apktool/install/ and place it in same folder as apktool.jar

- create a working folder in your drive to store apk file and its decompressed contents e.g. in C:\APK

- place vlc.apk into C:\APK (PLEASE USE OLD VERSION OF VLC, DO NOT USE NEWER THAT 3.4.0, I RECOMMEND TO USE VERSION 3.2.6, BECAUSE NEWER VERSION WILL GIVE SLOW PERFORMANCE)

- you can get vlc.apk from here : https://download.videolan.org/pub/videolan/vlc-android/

- open Command Prompt (cmd.exe) and goto C:\APK

- to decompress/decompile vlc.apk type :
```
apktool d vlc.apk -o vlc_mod
```

- replace youtube.lua in C:\APK\apk_mod\assets\lua\playlist\ with the latest youtube.lua from https://code.videolan.org/videolan/vlc/-/raw/master/share/lua/playlist/youtube.lua

- to rebuild apk type :
```
apktool b vlc_mod -o vlc_mod.apk
```

- open APK EDITOR STUDIO, click Tools -> Key Manager to create a sign key

- in Keystroke Path input : C:/APK/MyKey.jks then click Create

- input those all necessary fields (passwords, key name alias,  your name & address, etc) then click OK, then close APK EDITOR STUDIO

- now you can sign that vlc_mod.apk by right click on it Windows Explorer and click Sign APK then install it on your android device

- if you're still insist to use latest version of VLC.APK you need to add --use-aapt2 in apktool command line e.g :
```
apktool d --use-aapt2 vlc.apk -o vlc_mod
```
- and to build apk :
```
apktool b --use-aapt2 vlc_mod -o vlc_mod.apk
```


For LOCAL VIDEOS on your STORAGE, you need ES MEDIA PLAYER (by installing ES FILE EXPLORER) or FX MEDIA PLAYER (by installing FX FILE MANAGER)

This is modified version of my previous app https://github.com/botbahlul/Live-Subtitle (on the TRANSLATION METHODE) which on this version I use ONLINE Google Translate API

Check my other SPEECH RECOGNITIION + TRANSLATE PROJECTS in https://github.com/botbahlul/botbahlul.github.io
