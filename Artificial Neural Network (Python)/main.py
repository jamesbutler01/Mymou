import os.path
import time
import numpy as np
np.seterr(all='ignore')


# Maths functions

# softmax (output layer)
def softmax(w):
    e = np.exp(w - np.amax(w))
    dist = e / np.sum(e)
    return dist


# tanh for feedforward
def tanh(x):
    return np.tanh(x)


# tanh derivative for backprop
def dtanh(y):
    return 1 - y * y


class ANN(object):
    def __init__(self, inputSize, hiddenlayersize, outputlayersize, iterations, learning_rate,
                 momentum, rate_decay):
        # initialize parameters
        self.iterations = iterations
        self.learning_rate = learning_rate
        self.layertwo_input = 0
        self.layertwo_output = 0
        self.momentum = momentum
        self.rate_decay = rate_decay

        # initialize arrays
        self.size_input = inputSize + 1  # add 1 for bias node
        self.size_hidden = hiddenlayersize
        self.size_output = outputlayersize
        self.activation_input = np.ones(self.size_input)
        self.activation_hidden = np.ones(self.size_hidden)
        self.activation_output = np.ones(self.size_output)

        # Use random weights as start for input and hidden layers
        input_range = 1.0 / self.size_input ** (1 / 2)
        self.weights_input = np.random.normal(loc=0, scale=input_range, size=(self.size_input, self.size_hidden))
        self.weights_hidden = np.random.uniform(size=(self.size_hidden, self.size_output)) / np.sqrt(self.size_hidden)

        # create arrays of 0 for changes
        # this is essentially an array of temporary values that gets updated at each iteration
        # based on how much the weights need to change in the following iteration
        self.ci = np.zeros((self.size_input, self.size_hidden))
        self.co = np.zeros((self.size_hidden, self.size_output))

    def feedForward(self, inputs):
        # Get input activations
        self.activation_input[0:self.size_input - 1] = inputs

        # Multiply input layer by weights and apply non-linear tanH function
        sum = np.dot(self.weights_input.T, self.activation_input)
        self.activation_hidden = tanh(sum)

        # Multiply hidden layer by weights and apply non-linear softmax function
        sum = np.dot(self.weights_hidden.T, self.activation_hidden)
        self.activation_output = softmax(sum)

        return self.activation_output

    def backPropagate(self, targets):
        # calculate error terms for output
        # the delta (theta) tell you which direction to change the weights
        output_deltas = -(targets - self.activation_output)

        # calculate error terms for hidden
        error = np.dot(self.weights_hidden, output_deltas)
        hidden_deltas = dtanh(self.activation_hidden) * error

        # update the weights connecting hidden to output, change == partial derivative
        change = output_deltas * np.reshape(self.activation_hidden, (self.activation_hidden.shape[0], 1))
        regularization = self.layertwo_output * self.weights_hidden
        self.weights_hidden -= self.learning_rate * (change + regularization) + self.co * self.momentum
        self.co = change

        # update the weights connecting input to hidden, change == partial derivative
        change = hidden_deltas * np.reshape(self.activation_input, (self.activation_input.shape[0], 1))
        regularization = self.layertwo_input * self.weights_input
        self.weights_input -= self.learning_rate * (change + regularization) + self.ci * self.momentum
        self.ci = change

        # calculate error
        error = -sum(targets * np.log(self.activation_output))

        return error

    def trainNetwork(self, labelledTrainingData, validationInput, validationLabels):
        # Train the network on labelledTrainingData

        # Repeat process for number times specified by iterations
        for i in range(self.iterations):
            error = 0.0
            np.random.shuffle(labelledTrainingData)

            # Iterate through entire training set
            for p in labelledTrainingData:
                inputs = p[0]
                targets = p[1]

                self.feedForward(inputs)
                error += self.backPropagate(targets)

            # Adjust learning rate to slow down as iterations increase
            self.learning_rate = self.learning_rate * (
            self.learning_rate / (self.learning_rate + (self.learning_rate * self.rate_decay)))

            # Check current accuracy of model on the test data
            if i % 5 == 0:
                self.predict(validationInput, validationLabels)

        # Training finished - save the weights to be used with the Android system
        np.savetxt("wi.txt", self.weights_input, delimiter=',')
        np.savetxt("wo.txt", self.weights_hidden, delimiter=',')

    def predict(self, X, labels):
        # Compare model's predictions to labels

        # Iterate through data to get prediction for each entry
        predictions = []
        for p in X:
            predictions.append(self.feedForward(p))

        # Convert predictions to correct format
        predictions = np.vstack(predictions)  # List to array
        boolPredictions = np.zeros_like(predictions)
        boolPredictions[np.arange(len(predictions)), predictions.argmax(1)] = 1  # Now boolean predictions

        # Convert labels to appropriate format
        labelArray = np.vstack(labels)

        # Get accuracy of predictions by comparing predictions to labels
        # NB: If more than two targets boolean matching will need to be changed
        correct = labelArray == boolPredictions
        correct = np.delete(correct, 0, axis=1)
        print("Predicted data with", np.mean(correct)*100, "% success")


if __name__ == '__main__':
    # User defined variables
    varInputSize = 30552  # Neurons in input layer (1 per pixel in image)
    varHiddenLayerSize = 20  # Neurons in hidden layer
    varOutputSize = 2  # Neurons in output layer
    varMomentum = 0.0001  # To help avoid local minima
    varIterations = 15  # Number time to run all training data through model
    varRateDecay = 0.01  # Reduces learning rate by this factor as iterate through training
    varLearningRate = 0.01  # Amount to adjust the weights by on each step
    varNumSubjects = 2  # Number of subjects
    trainingToValidationRatio = 0.8  # Ratio of data to be kept in training set vs validation set

    def load_data(numSubjects):
        fname = 'labeledData.npy'
        if os.path.isfile(fname):
            data = np.load(fname)
        else:
            raise Exception('No data found, run PrepareData.py first to generate labelled dataset')
        # Split input into data and label
        labels = data[:, 0:numSubjects]  # Labels are at front of data
        data = data[:, numSubjects:]  # Rest of array is the data

        # Normalise data and save values to use on future inputs to ANN
        mean = data.mean()
        var = data.var()
        data = (data - mean) / var
        meanAndVar = np.array([mean, var])
        np.savetxt("meanAndVar.txt", meanAndVar)

        # Finally zip up data into a list of labels and data
        output = []
        for i in range(data.shape[0]):
            tempList = list((data[i, :].tolist(), labels[i].tolist()))
            output.append(tempList)

        return output

    # Get data to start the training
    start = time.time()
    X = load_data(varNumSubjects)

    # Shuffle data and split into training and validation groups
    np.random.shuffle(X)
    n = np.shape(X)[0]
    trainingX = np.copy(X[:int(n*trainingToValidationRatio)])
    validationX = np.copy(X[int(n*trainingToValidationRatio):])
    valInput = [i[0] for i in validationX]
    valLabels = [i[1] for i in validationX]

    # Initialise and train model
    model = ANN(varInputSize, varHiddenLayerSize, varOutputSize, varIterations, varLearningRate, varMomentum, varRateDecay)
    model.predict(valInput, valLabels)  # Test trained model on validation data
    model.trainNetwork(trainingX, valInput, valLabels)
    model.predict(valInput, valLabels)  # Test trained model on validation data

    print(f'Finished in {int(time.time()-start)}s')

