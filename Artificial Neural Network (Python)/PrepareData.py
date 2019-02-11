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
numsubjects = len(folders)

def makeCSV():
    # Get list of all images in folder
    list_of_photos = []
    for dirpath, dirnames, filenames in os.walk(mypath):
        for filename in [f for f in filenames if f.endswith(".jpg")]:
            list_of_photos.append(os.path.join(dirpath, filename))

    counts = [0 for _ in range(numsubjects)]

    # For all photos
    for i, photo_path in enumerate(list_of_photos):

        # Find corresponding float array for each figure
        floatPath = photo_path[:-25] + "f\\f" + photo_path[-23:-4] + ".txt"

        if not os.path.isfile(floatPath):
            raise Exception('Error! No matching .txt file found!')

        # Load file and get label from folder name
        temp_arr = np.loadtxt(floatPath)
        label = photo_path[-25:-24]

        # Make one hot array
        one_hot_arr = np.zeros(numsubjects)
        subj = folders.index(label)
        one_hot_arr[subj] = 1
        counts[subj] += 1

        # Append label to front of image
        temp_arr = np.hstack([one_hot_arr, temp_arr])

        try:

            arr = np.vstack([arr,temp_arr])

        except NameError:

            arr = np.copy(temp_arr)

        if i % 10 == 0:

            print(f'Sorting photos, progress: {i}/{len(list_of_photos)}')

    print('Saving data..')
    np.save("labeledData.npy",arr)
    print('Data saved')


# Deletes photos such that all subjects have same number of photos
def MakeSameNumberPhotosPerSubject():

    # Get list of photos in each folder
    photodirs = [[] for _ in range(numsubjects)]
    for photodir, subjPath in zip(photodirs, folders):
        for file in os.listdir(mypath+subjPath):
            if file.endswith(".jpg"):
                photodir.append(os.path.join(mypath+subjPath, file))

    # Find subject with fewest photos
    num_photos_per_subj = [len(photo) for photo in photodirs]
    min_num_photos = np.min(num_photos_per_subj)

    count = 0
    for photodir in photodirs:

        # Loop until reach min_num_photos
        while len(photodir) != min_num_photos:

            # Pick a random photo and delete it
            randphoto = np.random.randint(0, len(photodir))
            os.remove(photodir[randphoto])

            # Remove entry from list
            del(photodir[randphoto])
            count += 1

    print(count, "items deleted")


# Remove any .txt files that don't have a matching photo in directory
def RemoveUnwantedFloatArrays():
    photoList = []
    for dirpath, dirnames, filenames in os.walk(mypath):
        for filename in [f for f in filenames if f.endswith(".jpg")]:
            photoList.append(filename[:-4])

    floatList = []
    floatPaths = []
    for dirpath, dirnames, filenames in os.walk(mypath):
        for filename in [f for f in filenames if f.endswith(".txt")]:
            floatList.append(filename[1:-4])
            floatPaths.append(os.path.join(dirpath, filename))

    count = 0
    for floatname, floatpath in zip(floatList, floatPaths):
        if floatname not in photoList:
            if os.path.isfile(floatpath):
                os.remove(floatpath)
                count += 1

    print(count, "items deleted")

if __name__ == '__main__':
    MakeSameNumberPhotosPerSubject()
    RemoveUnwantedFloatArrays()
    makeCSV()
