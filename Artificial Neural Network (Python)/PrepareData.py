import os
import numpy as np

"""

Run on folder that contains folders 'O' and 'V' and 'f', where 'O' and 'V' contain selfies corresponding to each 
subject and 'f' contains the corresponding integer arrays for all the images found in folders 'O' and 'V'

Function will then sort through each image and appends them into a single 2D matrix with 1 row per image with the
first two points of each row correspond to a 1-hot label for the identity of that image

"""

mypath = "raw/"  # Location of files that contains one folder of all integer arrays, and one folder per subject
# containing the .jpg files for that subject
folder1name = 'O'  # Name of folder containing images for subject1
folder2name = 'V'  # Name of folder containing imges for subject2
folders = (folder1name, folder2name)

def makeCSV():
    # Get list of all images in folder
    f = []
    for dirpath, dirnames, filenames in os.walk(mypath):
        for filename in [f for f in filenames if f.endswith(".jpg")]:
            f.append(os.path.join(dirpath, filename))

    subject1count = 0
    subject2count = 0

    # For all photos
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

        print(f'Progress: {i}/{len(f)}  Subject {folder1name}: {subject1count}, Subject {folder2name}:{subject2count} {floatPath}')

    np.save("labeledData.npy",arr)

    print("Done!")


def MakeSameNumberPhotosPerSubject():
    # Deletes photos from one subject to make both subjects have same number of photos
    photonames = [[], []]
    photodirs = [[], []]

    # Get list of photos in each folder
    for i, (photoname, photodir, subjPath) in enumerate(zip(photonames, photodirs, folders)):
        for file in os.listdir(mypath+subjPath):
            if file.endswith(".jpg"):
                photodir.append(os.path.join(mypath+subjPath, file))
                photoname.append(file)

    # Remove random photos until even number in each folder
    numphotos = [len(photo) for photo in photonames]
    maxphotos = np.argmax(numphotos)
    diff = np.abs(np.diff(numphotos))
    print(f'Deleting {diff} photos')
    for _ in range(diff):
        randphoto = np.random.randint(0, len(photodirs[maxphotos]))
        os.remove(photodirs[maxphotos][randphoto])
        del(photodirs[maxphotos][randphoto])


if __name__ == '__main__':
    makeCSV()
