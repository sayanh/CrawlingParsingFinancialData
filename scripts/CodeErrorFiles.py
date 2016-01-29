import os
from os import listdir

def main():
    root = "D:/work/TUM/study/IDP/samplelog files"
    destErrFile = "D:/work/TUM/study/IDP/ErrorFilesStringoutofb.txt"
    fError = open(destErrFile, 'w')
    filelist=os.listdir(root)
    #fileOpen = "D:/work/TUM/study/IDP/samplelog files/ParsingHistory.log2015_01_11_21_03_56"
    for fileOpen in filelist:
        fileOpen = root + "/" + fileOpen
        print "processing " + fileOpen + "....."

        lines = tuple(open(fileOpen, 'r'))
        timedOutLines = ""
        for line in lines:
            if ("Timed out" in line):
                timedOutLines = timedOutLines + line

        for line in lines:
            if ("Started" in line):
                tmpArr = line.split("\\")
                fileName = tmpArr[len(tmpArr) - 1]
                fileName = fileName[0:len(fileName)-1]

                #print (tmpArr[len(tmpArr) - 1])
                found = False
                for line2 in lines:
                    if (fileName in line2 and "Completeinformation" in line2):
                        found = True
                #print found
                if (not(found) and not(fileName in timedOutLines)):
                    for line4 in lines:
                        if (fileName in line4 and "ERROR" in line4):
                            print line4
                            if ("file " in line4):
                                tempArr = line4.split("file ")
                                tempArr1 = tempArr[1].split(" |")
                                output = fileOpen + "|" + tempArr1[0].strip() + "|" + line4
                                print output
                                fError.write(output)
                                fError.flush()


    fError.close()


if __name__ == '__main__':
    main()



