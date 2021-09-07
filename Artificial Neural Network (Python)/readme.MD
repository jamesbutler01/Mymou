# Python code for facial recognition module



## Preparing data (PrepareData.py)

##### Data should be in the format: [ (one hot array label) (image array generated with the Mymou system) ]

- E.g. 
- [0, 1, 255, 213, 24, 89,...]  # Subject 1
- [1, 0, 39, 210, 130, 198,..]  # Subject 2

##### This can be generated using the PrepareData.py script which runs in a folder that contains:
- 1 folder containing all images of subject 1
- 1 folder containing all images of subject 2
- 1 folder containing all integer arrays that correspond to all the images in the previous 2 folders

##### The script then sorts through all the integer arrays, finds whether the corresponding .jpg is in folder1 or folder2 and appends an appropriate one hot label to the array

##### All this data is then stacked and saved as one big file which will run with the main artifical neural network script.

##### Note: There are two sets of photos provided, as different tablets use different resolution photos
 - Photos to use with the 2016 Samsung Galaxy Tab A 10.1" can be found in /raw
 - Photos to use with the 2019 Samsung Galaxy Tab A 10.1" can be found in /raw2



## Training the neural network (main.py)

#### Runs with the sample data generated with PrepareData.py

#### Does the following:
- Read in the data and split x and y data
- Normalises data and saves normalisation parameters which are used by the Mymou system for online image identification
    - Saves them as meanAndVar.txt
- Splits data into a training set and test set at a ratio specified by the user
- Trains the network with parameters specified by the user
- Saves the trained weights which can then be used by the Mymou system for online image identification
    - wi.txt - weights for the input-hidden layer
    - wo.txt - weights for the hidden-output layer

## Using the trained network with the Mymou app
- Generate wi.txt, wo.txt and meanAndVar.txt files as described above
- Transfer these three files to the Mymou folder on your tablet (root/Mymou)
- Switch on FaceRecog in System settings -> System settings
- Set number of monkeys to 2 in System settings -> Task settings
- Choose a task that actually uses the go cue (it is disabled in many of the pretraining tasks), e.g. Spatial Response task
- Start the task
- You'll then see two go cues appear (appearance can be changed in settings), one for each monkey trained to be identified by the network
- Each monkey must then press their specific Go cue in order to start a trial 
