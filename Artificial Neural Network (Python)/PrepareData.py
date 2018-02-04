import os
import numpy as np


def makeCSV():
    mypath = "C:\\path\\"  # Location of files that contains one folder of all integer arrays, and one folder per subject
    # containing the .jpg files for that subject
    folder1name = '1'  # Name of folder containing images for subject1
    folder2name = '2'  # Name of folder containing imges for subject2

    # Get list of all images in folder
    f = []
    for dirpath, dirnames, filenames in os.walk(mypath):
        for filename in [f for f in filenames if f.endswith(".jpg")]:
            f.append(os.path.join(dirpath, filename))

    subject1count = 0
    subject2count = 0
    for i in range(len(f)):
        #    Find corresponding float array for each figure
        floatPath = f[i][:-25] + "f\\f" + f[i][-23:-4] + ".txt"
        if os.path.isfile(floatPath):
            tempArr = np.loadtxt(floatPath)
            label = f[i][-25:-24]
            #    Add one shot to front
            if label is folder1name:
                oneshotArr = np.array([1, 0])
                subject1count += 1
            elif label is folder2name:
                oneshotArr = np.array([0, 1])
                subject2count += 1
            else:
                print("ERROR - NO LABEL FOUND!")

            #    Append label to front of image
            tempArr = np.hstack([oneshotArr,tempArr])
            print(i,"/",len(f)," ",tempArr.shape, " ", subject2count, "/", subject1count, floatPath)
            if i is 0:
                arr = np.copy(tempArr)
            else:
                arr = np.vstack([arr,tempArr])
        else:
            os.remove(f[i])  # Remove any integer arrays that don't have a corresponding image

    np.save("labeledData.npy",arr)

    print("Done!")


if __name__ == '__main__':
    makeCSV()
