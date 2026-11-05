# Plates Showroom - Android TV app

A full-screen app that opens the showroom website on an Android TV / Google TV.
The plates are NOT inside the app. They come live from the website, so changing plates in
Manage Inventory, or updating the website files, never needs a new APK.

## Build the APK (GitHub does it for you)
1. Put this whole folder in a GitHub repository (see the steps given in the chat).
2. Open the repository -> **Actions** tab -> **Build APK** -> wait about 5 minutes for the green tick.
3. Download the APK:
   - **Releases** (right side of the repository page) -> `PlatesShowroom.apk`, or
   - the finished run -> **Artifacts** -> `PlatesShowroom-APK` (a zip containing the APK).

## Change something later
- Website address: `app/src/main/res/values/strings.xml` -> `start_url`
- Name under the icon: same file -> `app_name`
- After editing a file on GitHub, the build runs again by itself and makes a new APK.
