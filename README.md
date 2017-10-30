
### Android secure shared preferences using Conceal (Facebook)

[![](https://jitpack.io/v/KaKaVip/secure-preferences.svg)](https://jitpack.io/#KaKaVip/secure-preferences)

* Conceal -> https://github.com/facebook/conceal

![](https://i.gyazo.com/e7dbb57aea8fdc77eafd767e25bb2cb2.png)

##### Why use ?
- Most of security for data ( as user token, some api key ...)
- Less SharedPreferences file size
- Base on SharedPreferences default, to easy use
- Faster encrypt with the Conceal
- `256-bit` encryption

### We're supported Conceal 2.x (faster than old version)

Download
-------
#####Gradle:

Step 1. Add it in your root build.gradle at the end of repositories:

```groovy
allprojects {
	repositories {
		...
		maven { url "https://jitpack.io" }
	}
}
```

Step 2. Add the dependency
```groovy
dependencies {
        compile 'com.github.KaKaVip:secure-preferences:1.1.0'
        compile 'com.facebook.conceal:conceal:2.0.1@aa'
}
```


How to use
-------
In Application: `Data return empty if you don't setting this`

Since v2.0.+ (2017-06-27) you will need to initialize the native library loader. This step is needed because the library loader uses the context. The highly suggested way to do it is in the application class onCreate method like this:

```java
    @Override
    public void onCreate() {
        super.onCreate();
        SecurePreferences.init(this); 
    }
```
When used: 
```java
SharedPreferences prefs = new SecurePreferences.Builder(MainActivity.this)
                .password("password")
                .filename("settings")
                .build();
// Save token
prefs.edit().putString("token","f93ce38ab841fad01bc5e571ea861e9e").apply();

// Get Token
String token = prefs.getString("token","if_not_found");

```


See `settings.xml` file:
```XML

// SharedPreferences default
<string name="token">f93ce38ab841fad01bc5e571ea861e9e</string>

// Via SecurePreferences
<string name="94a08da1fecbb6e8b46990538c7b50b2">AQI80a7LVHPIW8l00ecIag95oKcaje6U2fgREyvUPTnn3OCBK5rq/xACPjI9</string>

```


License
-------

    Copyright 2016 Pham Quy Hai, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
