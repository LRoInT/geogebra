# Welcome to GeoGebra!


This repository contains source code of [GeoGebra](https://www.geogebra.org)'s math apps.
It is available on a private GitLab instance and mirrored to GitHub.

Please read https://www.geogebra.org/license about GeoGebra's
licensing.

## Running the web version
To start the web version from command line, run

```
./gradlew :web:run
```

This will start a development server on your machine where you can test the app. 
If you need to access the server from other devices, you can specify a binding address

```
./gradlew :web:run -Pgbind=A.B.C.D
```

where `A.B.C.D` is your IP address. 
Then you can access the dev server through `http://A.B.C.D:8888`.
You can also run `./gradlew :web:tasks` to list other options.

## Running the desktop version (Classic 5)
To start the desktop version from command line, run

```
./gradlew :desktop:run
```
You can also run `./gradlew :desktop:tasks` to list other options.

## Setup the development environment

1. You need a `Java 11` to run this proggramme.
2. If there is no `jre` in your Java 11, use `./bin/jlink --module-path jmods --add-modules java.desktop --output jre` to create it.
3. Set the path of `Java 11` in `$JAVA_HOME`, maybe set it in `gradlew` or `gradlew.bat` is simple. The path should have `jre` and `bin` ddirectory.

### Run in Web
1. Use `gradlew :web:run`
2. When the GWT GUI show, you should choos the MTML file in the windows(In the upper middle position of the windows). Then wait, the browser windows will open, and it need to load for a time.
3. You can lanuage in Global Setting(Click the setting bottom, theen click first bottom in the right)

### Run in Desktop
1. Use `gradlew :desktop:run`
