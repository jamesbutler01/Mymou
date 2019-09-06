# Python code for facial recognition module

## Preparing data (PrepareData.py)

#### Data should be in the format: [ (one hot array label) (image array generated with the Mymou system) ]

- E.g. 
- [0, 1, 255, 213, 24, 89,...]  # Subject 1
- [1, 0, 39, 210, 130, 198,..]  # Subject 2

#### This can be generated using the PrepareData.py script which runs in a folder that contains:
- 1 folder containing all images of subject 1
- 1 folder containing all images of subject 2
- 1 folder containing all integer arrays that correspond to all the images in the previous 2 folders

#### The script then sorts through all the integer arrays, finds whether the corresponding .jpg is in folder1 or folder2 and appends an appropriate one hot label to the array

#### All this data is then stacked and saved as one big file which will run with the main artifical neural network script.


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

#### Has various parameters which can be adjusted by the user to alter performance of the network:
- varInputSize = 30552  # Neurons in input layer (1 per pixel in image)
- varHiddenLayerSize = 20  # Neurons in hidden layer 
- varOutputSize = 2  # Neurons in output layer (1 per subject)
- varMomentum = 0.0001  # To help avoid local minima
- varIterations = 50  # Number of times to run all training data through model
- varRateDecay = 0.01  # Reduce learning rate by this factor as iterate through training
- varLearningRate = 0.01  # Amount to adjust the weights by on each step
- varNumSubjects = 2  # Number of subjects
- trainingToValidationRatio = 0.85  # Ratio of data to be kept in training set vs validation set
