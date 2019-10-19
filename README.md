# Accel2FA

This is an application that allows a user to login with 2FA utilising ambient sound and accelerometer data.

### Prerequisites

```
Minimum version of Android Oreo (SDK26) required.
```

### Installing

```
1. Build this application on Android Studio or download a prebuilt APK from release.
```

### How to use

```
1. nc -l 50505 on your local machine.
2. Enter IP of the local machine and press start to start recording.
3. Press stop to end recording and data will be sent to the listening machine and application will attempt to play the ambient noise recorded.

DEBUG purpose
1. nc [phone ip] 5300
2. Send "TEST"
3. Phone will start recording for 3 seconds and audio will be played.
```

### To-dos

``` 
1. Ability to receive push notification and data from browser.
2. Compare similarity between audio.
3. Keystroke comparison.
```
