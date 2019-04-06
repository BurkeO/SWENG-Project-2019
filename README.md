# SWENG-Project-2019
Repo for the group programming project of 2019 @ TCD  
---- Add Description of how to get project up and running with other accounts tied to watson and firebase ----


Firebase setup:
This is required if you wish to link the app to a different firebase account.
1. Go to http://firebase.google.com/
2. Sign in with an email to get started
3. Go to https://console.firebase.google.com/ and you should be greeted with a screen like this : https://github.com/BurkeO/SWENG-Project-2019/blob/master/Documentation/FirebaseSetup/StartScreen.png
4. Press on add project : https://github.com/BurkeO/SWENG-Project-2019/blob/master/Documentation/FirebaseSetup/AddProject.png
5. Fill out the form, naming the project (this can be anything) and choosing what region you want the database hosted in.
6. Once you have created the project, you should arrive at the projects overview page.
7. Press on the add app button : https://github.com/BurkeO/SWENG-Project-2019/blob/master/Documentation/FirebaseSetup/ProjectDashboard.png
8. Choose android as the platform
9. Next you will see a form where you need to add the package name of the app, you can find this in android studio, for this app it is com.example.vmac.WatBot : https://github.com/BurkeO/SWENG-Project-2019/blob/master/Documentation/FirebaseSetup/RegisterApp.png
10. Once you’ve added the app and debug signing certificate (optional), you now need to download the google-services.json file, and replace the existing one in the project in the directory shown in the screenshot : https://github.com/BurkeO/SWENG-Project-2019/blob/master/Documentation/FirebaseSetup/AddingJson.png
11. After this the app should be up and running with the new firebase account. 

Keep in mind the website may change in the future but as of 6/4/19 this is the proceedure required.

This is currently how the database looks, messages that have been exchanged can be found by expanding the chatRooms section : https://github.com/BurkeO/SWENG-Project-2019/blob/master/Documentation/FirebaseSetup/Database.png
