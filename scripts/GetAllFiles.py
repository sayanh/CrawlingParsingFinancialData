__author__ = 'Anarchy'
import os
import time

def main():
    sourceDir = "E:\IDP\Extracted Files"
    traverse(sourceDir)



def traverse(sourceDir):
    '''fileList = os.listdir(sourceDir)
    for file in fileList:
        if (file.)
        print file'''
    fwrite = open("D:/work/TUM/study/IDP/Alldonwloaded_files.txt", 'w')
    for root, dirs, files in os.walk(sourceDir):
        #print root
        #print (len(path) - 1) *'---' , os.path.basename(root)
        for file in files:
            fileName = root + "\\" +  file
            fwrite.write(fileName)
            fwrite.flush()
            #time.sleep(1)
    fwrite.close()

if __name__ == '__main__':
    main()