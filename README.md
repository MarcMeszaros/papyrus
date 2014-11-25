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
1. Install the Android 4.0.3+ (API 15) and Android Support Repository from the SDK tools

## Gradle/CLI ##
1. Done. Run some gradle commands. :)

# Basic Gradle Commands #
Below are some basic Gradle commands to do some common tasks. (Assumes you are in the project root directory.)

    # debug build, with unit tests
    ./gradlew build

    # debug build, no unit tests (faster)
    ./gradlew assembleDebug

    # release build, no unit tests (keystore and release.properties file must exist)
    ./gradlew assembleRelease
