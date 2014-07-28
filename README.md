=== TINKER ===

Tinker is a social networking application that allows people to find other people in their local area with similar interests. 
It is similar to Tinder, but oriented toward making friends instead of finding dates.

=== Developers ===

Todd Ryks
Logan Isitt
Arash Afshar

=== Features ===

* Users may display interest in another user by swiping right on their profile. Swiping left signifies no interest in the user.
* Sign in using Facebook, information is pulled from Facebook profile to Tinker profile (only username and profile picture unless access is granted on our developer page, however).
* Saved users can be removed by holding one's finger down on a name in the saved users list and clicking remove in the context menu.
* Users can modify their Tinker profile at any time, without affecting their Facebook profile.
* Users may chat with other users who they have displayed interest in.
* Users can browse through local Tinker users. Coordinate information is pulled from Google, based on the location provided on the profile, and the distance between users is approximated in the Parse Cloud with the Haversine formula.

=== SETUP ===

Clone the repository into your android workspace with the command "git clone https://github.com/ArashyNole/Tinker.git"

Make sure you have the Android 2.2 SDK Platform in the SDK Manager. If not, install it.

Then, in Eclipse, go to File → Import…

Click General → Existing Projects into Workspace

Make sure the "Select root directory" radio button is selected, and browse to your workspace directory.

In the Projects box, check the box next to Tinker and click Finish

There will still be library problems at this point. The Facebook SDK is necessary to compile the project in Eclipse, as are Sinch and Parse library files. An APK file is included in the root directory of the project to make .

=== SOURCES ===

This Parse tutorial was used as the base of our project to get Facebook authentication working quickly:

https://parse.com/tutorials/integrating-facebook-in-ios

This is a tutorial on making Sinch work with Parse to make a chat application:

http://sinch.github.io/android-messaging-tutorial/

The function used to do geocoding was pulled from here:

https://stackoverflow.com/questions/5205650/geocoder-getfromlocation-throws-ioexception-on-android-emulator

The Haversine function at this website was used to calculate distance in the backend:

http://jsperf.com/vincenty-vs-haversine-distance-calculations
