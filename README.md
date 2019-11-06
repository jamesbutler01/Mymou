# Mymou: A standalone touchscreen in-cage testing device

##### All files needed to run the system can be found in this repository:

### 1) Reward Delivery Interface (Arduino)
- A simple script that runs on an Arduino Uno board
- It receives Bluetooth commands and then switches on or off digital out channels on the Arduino board as required
- These digital output channels can then be connected to your reward delivery system (e.g. peristaltic pump, pellet dispenser) to allow automated reward administration

### 2) Artifical Neural Network (Python)
- This is for training the system for facial recognition
- Runs in Python 3.5
- Takes image files collected with the Mymou system and then tunes the neural network to correctly identify the subject from an image
- Should work up to 99% accuracy

### 3) Behavioural Testing Unit (Android)
- Designed to run on Android 6.0.0. (Samsung Galaxy Tab A 10.1")
- Delivers the task to the subjects
- Can be easily programmed to deliver any tasks desired by the experimenter
- Example tasks are provided
- Uses Bluetooth to control the Reward Delivery Interface
- Also has built in facial recognition to identify which subject is playing when 

### 4) Tablet Holder (OpenSCAD)
- A scale 3D model of the tablet holder used to mount the tablet to the side of the cage

##### Detailed information for each of these parts of the system can be found in the respective folders
