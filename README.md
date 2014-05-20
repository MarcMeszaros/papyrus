# Overview #
Papyrus enables users to create a virtual library of all the books
they own. Users can also keep track of loans they make to other people.

Users can add books in a variety of ways. The easiest way is to use the barcode scanning
functionality to scan the ISBN barcode. Papyrus attempts to get the book
information from the Google Books service.

Multiple libraries are supported by Papyrus and so are multiple copies of books.
This feature gives the user finer grained control over how they manage their
books.

# Getting Started #
1. Make sure to uninstall/reinstall the Android SDK (SDK folder/file layouts have changed and setup is based on SDK r22+)
2. Install the Android 2.3.3 (API 10) and Android Support Package from the SDK tools
3. Proceed with any of the setup instructions below

## Gradle/CLI ##
1. Done. Run some maven commands. :)

# Basic Gradle Commands #
Below are some basic Maven commands to do some common tasks. (Assumes you are in the project root directory.)

    # debug build, with unit tests
    ./gradlew build

    # debug build, no unit tests (faster)
    ./gradlew assembleDebug

    # release build, no unit tests (keystore and release.properties file must exist)
    ./gradlew assembleRelease
