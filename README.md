# SWENG-Project-2019
Repo for the group programming project of 2019 @ TCD  
---- Add Description of how to get project up and running with other accounts tied to watson and firebase ----


## Firebase setup:
This is required if you wish to link the app to a different firebase account.
1. Go to [Firebase website](http://firebase.google.com/)
2. Sign in with an email to get started
3. Go to [Firebase Console](https://console.firebase.google.com/) and you should be greeted with a screen like [this]( https://github.com/BurkeO/SWENG-Project-2019/blob/master/Documentation/FirebaseSetup/StartScreen.png)
4. Press on [add project](https://github.com/BurkeO/SWENG-Project-2019/blob/master/Documentation/FirebaseSetup/AddProject.png)
5. Fill out the form, naming the project (this can be anything) and choosing what region you want the database hosted in.
6. Once you have created the project, you should arrive at the projects overview page.
7. Press on the add app [button]( https://github.com/BurkeO/SWENG-Project-2019/blob/master/Documentation/FirebaseSetup/ProjectDashboard.png)
8. Choose android as the platform
9. Next you will see a form where you need to add the package name of the app, you can find this in android studio, for this app it is com.example.vmac.WatBot : [example](https://github.com/BurkeO/SWENG-Project-2019/blob/master/Documentation/FirebaseSetup/RegisterApp.png)
10. Once you’ve added the app and debug signing certificate (optional), you now need to download the google-services.json file, and replace the existing one in the project in the directory shown in the [screenshot](https://github.com/BurkeO/SWENG-Project-2019/blob/master/Documentation/FirebaseSetup/AddingJson.png)
11. After this the app should be up and running with the new firebase account. 

Keep in mind the website may change in the future but as of 6/4/19 this is the proceedure required.

This is currently how the database looks, messages that have been exchanged can be found by expanding the chatRooms section : https://github.com/BurkeO/SWENG-Project-2019/blob/master/Documentation/FirebaseSetup/Database.png


## IBM Watson setup:
This is required if you wish to link the app to a different IBM account.
1. Go to [IBM Watson Assistant page](https://cloud.ibm.com/catalog/services/watson-assistant).
2. Sign up for a free IBM Cloud account or log in.
3. Click **Create**. 
4. After you create a Watson Assistant service instance, you land on the Manage page of the service dashboard. Take note of **API key** and **URL** provides. Put this API key into the *config.xml* file in *app/src/main/res/values/* under *assistant_apikey* and URL under *assistant_url*.
5. Click **Launch tool**. If you're prompted to log in to the tool, provide your IBM Cloud credentials.
6. From the home page of the Watson Assistant tool, click **Create a Skill**.
7. When a skill is created, add Intents and Dialogues to it. On the home page, you can find video tutorials made by IBM that show how to create a working assistant.
8. When assistant is ready, go back to **Skills** and switch to **Assistants** page. 
9. Create new Assistant and add the skills just created to it. Under **View API Details** for the assistant, make note of the **Assistant ID**. Insert this into *config.xml* under *assistant_id*.
10. The app will now call the new assistant created.
NB. This is valid as of 07/04/2019. Please, visit IBM Watson website for any updates and changes.

## IBM Watson Text-to-Speech and Speech-to-Text
1. Go to [IBM Cloud Catalog](https://cloud.ibm.com/catalog).
2. Select [Speech-to-Text](https://cloud.ibm.com/catalog/services/speech-to-text) and create a new service.
3. Click **Service credentials** on the left pane and click **New credential** to add a new credential. 
4. Click **View Credentials** to see the credentials. Make note of the **API Key** and **URL**. Put this API key into the *config.xml* file in *app/src/main/res/values/* under *STT_apikey*. Put URL into the same file under *STT_url*.
5. Do the same for [Text-to-Speech](https://cloud.ibm.com/catalog/services/text-to-speech) service. Put the API key into the *config.xml* file in *app/src/main/res/values/* under *TTS_apikey*. Put URL into the same file under *TTS_url*.
6. The app should now be running with the newly-created services.
