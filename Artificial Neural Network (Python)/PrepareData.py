import os
import numpy as np


def makeCSV():
    mypath = "raw/"  # Location of files that contains one folder of all integer arrays, and one folder per subject
    # containing the .jpg files for that subject
    folder1name = 'O'  # Name of folder containing images for subject1
    folder2name = 'V'  # Name of folder containing imges for subject2

    # Get list of all images in folder
    f = []
    for dirpath, dirnames, filenames in os.walk(mypath):
        for filename in [f for f in filenames if f.endswith(".jpg")]:
            f.append(os.path.join(dirpath, filename))

    subject1count = 0
    subject2count = 0

    # For all files
    for i in range(len(f)):

        # Find corresponding float array for each figure
        floatPath = f[i][:-25] + "f\\f" + f[i][-23:-4] + ".txt"

        if os.path.isfile(floatPath):

            # Load file and get label from folder name
            temp_arr = np.loadtxt(floatPath)
            label = f[i][-25:-24]

            # Add one hot to front
            if label is folder1name:
                one_hot_arr = np.array([1, 0])
                subject1count += 1
            elif label is folder2name:
                one_hot_arr = np.array([0, 1])
                subject2count += 1
            else:
                print("ERROR - NO LABEL FOUND!")

            # Append label to front of image
            temp_arr = np.hstack([one_hot_arr, temp_arr])

            if i == 0:
                arr = np.copy(temp_arr)
            else:
                arr = np.vstack([arr,temp_arr])

        else:

            os.remove(f[i])  # Remove any integer arrays that don't have a corresponding image

        print(i,"/",len(f)," ",temp_arr.shape, " ", subject2count, "/", subject1count, floatPath)

    np.save("labeledData.npy",arr)

    print("Done!")


if __name__ == '__main__':
    makeCSV()
