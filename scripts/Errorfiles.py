import sys
import re
import os
from os import listdir

#print 'Number of arguments:', len(sys.argv), 'arguments.'
root = "D:/work/TUM/study/IDP/samplelog files"
#sourceLogFile = "E:/IDP/ParsingHistory.log2015_01_11_21_03_56"
destCompleteFile = "D:/work/TUM/study/IDP/CompleteFiles_misnamed.txt"
destIncompleteFile = "D:/work/TUM/study/IDP/Incompletefiles_misnamed.txt"
#print 'Argument List:', str(sys.argv)
filelist=os.listdir(root)
linesComReadCount = 0
actualLinesCount = 0
comLinesCountList = []
inComLinesCountList = []
actualInLinesCount = 0

fcomplete = open(destCompleteFile, 'w')
f = open(destIncompleteFile, 'w')
for sourceLogFile in filelist:
    sourceLogFile=root + "\\" + sourceLogFile
    print "Processing file " + sourceLogFile + "....."
    lines = tuple(open(sourceLogFile, 'r'))
    #print str(lines)

    for line in lines:
        if ("Completeinformation" in line):
            fcomplete.write(line)


    #linesComRead = tuple(open(destCompleteFile, 'r'))

    with open(destCompleteFile) as foo:
        linesComReadCount = len(foo.readlines())
        #print "No. = " + str(linesComReadCount)
        actualLinesCount = linesComReadCount - actualLinesCount
        comLinesCountList.append(actualLinesCount)

    for line in lines:
        if ("Timed out" in line) :
            fileNameStr = line.split(' ', 5)


            fileNameArr = fileNameStr[5].split('|')
            fileName = fileNameArr[0]

            if (fileName not in lines):
                #print "Actual incomplete files =" + fileName + "\n"
                f.write(fileName + "\n")
                f.flush()




    with open(destIncompleteFile) as foo:
        linesInComReadCount = len(foo.readlines())
        #print "No. = " + str(linesInComReadCount)
        actualInLinesCount = linesInComReadCount - actualInLinesCount
        #print "incompl=" + str(actualInLinesCount)
        inComLinesCountList.append(actualInLinesCount)
f.close()
fcomplete.close()

print inComLinesCountList
print comLinesCountList
print filelist
print "program exits 1"
