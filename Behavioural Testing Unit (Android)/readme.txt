Behavioural Testing Unit (Android)

Designed and tested on Android 6.0.0.
Designed and tested on Samsung Galaxy Tab A 10.1"

This system can is used to control the Behavioural Testing Unit to deliver training tasks to the subjects
Runs continuously and can be configured what times of the day (and days of the week) to be active

Contains the following modules of interest:

Main Menu (MainMenu.java)
	Lightweight menu to start task
	Tells user whether bluetooth has successfully connected
	Also used to invoke necessary permissions for the system (Bluetooth, disk access, camera access)

Camera module (CameraMain.java)
	Instantiated when task starts
	Then runs instance of camera behind the task 
		Android security requirements makes you show user live image camera feed whenever using camera to prevent surreptious photo taking by a rogue application
		Circumvents this by hiding it behind the task - don't want subjects to see their own images
	This is also where you specify resolution of photo capture
		Currently configured to minimum resolution possible (176x144)

Save photos module (CameraSavePhoto.java)
	When task requests photo this grabs the next screen from the concurrently running CameraMain.java
	Then trims the photo down by the specified amount of pixels to leave just the centre area (as peripherary has no useful features for facial identification)
	It then converts the image file to a flatted 1D integer matrix with one point per pixel in the image
	Then feeds this through FaceRecog.java and notifies task of predicted subject ID
	Then saves
		The integer matrix in root:Mymou:_date_:f:_date_+_time_.txt (which can be collated to train the artifical neural network)
		The image file in root:Mymou:_date_:i:_date_+_time_.jpg (for offline verification of image identity)

Online facial recognition (FaceRecog.java)
	On the start of each trail takes a photo with the selfie camera
	It then converts the photo to flattened integer array and runs this through the artifical neural network
	Then receives the prediction and appends it to the trial log
	This prediction can also be used to change the task as desired on a per subject basis
	Asynchronous - runs on seperate thread to prevent interference with task

Bluetooth reward delivery system (MainMenu.java)
	WARNING: This means it wont run on an emulator (such as that provided by Android Studio) without Bluetooth capabilities
	Has all functions required provided which simply takes an integer as duration for reward to be administered (ms)
	Currently can control up to 4 different reward delivery system's simultaneously and independently  
	Needs configuring with your devices' UUID and address (lines 55 and 56)
	Embedded in the main menu code --> Had issues running it on its own thread 
		TODO: Change to own thread

Email alert capabilities (SendMail.java)
	Works with any gmail email account
	Input your account details 
	Can then email the specified account as desired
		e.g. automatic updates every x minutes
		e.g. alerts for events such as low battery

Data logging (LogEvent.java)
	Saves details for any crash that occurs in root:Mymou:_date_:_date_.txt
	Currently configured to one line per trial event
	Asynchronous - runs on seperate thread to prevent interference with task

Error monitoring (CrashReport.java)
	Stores one file per day in root:Mymou:_date_:crashReport.txt
	Includes time of crash, line of code that error occurred in etc
	Asynchronous - runs on seperate thread to prevent interference with task

Example task (TaskExample.java)
	Labelled example of a task we used to successfully teach NHPs associative learning of a large domain (5x5 grid with 4 edges per node)
	Showcases many features of the system and how to implement and use them 
		Photos as subjects play
		Bluetooth rewards
		Facial recognition
		Animations
		Timing functions (for secondary reinforcement, task paradigms etc)
	Currently configured to do the same start-end problem (to give the untrained user a chance to solve it!) but full functionality of the 5x5 environment is present
		Remove line 616 to disable this and gain full task functionality
