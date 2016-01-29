__author__ = 'Anarchy'
import time
import os


def main():
    source = "D:/work/TUM/study/IDP/misnamedtrial.txt"
    fileLines = tuple(open(source, 'r'))
    for fileName in fileLines:
        fileName = fileName[:-1]
        if (not("__" in fileName)):
            print "processing..."+ fileName
            exactFileNameArr = fileName.split('\\')
            exactFileName = exactFileNameArr[6]
            partsArr = exactFileName.split('_')
            part1 = partsArr[0]
            #print "part 1=" + part1
            newFinalName = ""
            print "exact file name = " + exactFileName
            fileContent = tuple(open(fileName, 'r'))
            for line in fileContent:
                line = line[:-1]
                if ("CONFORMED PERIOD OF REPORT:" in line):
                    print line
                    conformedDate = line[len("CONFORMED PERIOD OF REPORT: "):-1]
                    conformedDate = conformedDate.strip()
                    print "Conformed date=" + conformedDate
                    conformedYear = conformedDate[:4]
                    print "Conformed year=" + conformedYear
                    #time.sleep(5)
                elif ("FILED AS OF DATE:" in line):
                    print line
                    filedAsOfDate = line[len("FILED AS OF DATE: "):-1]
                    filedAsOfDate = filedAsOfDate.strip()
                    print "filed as of date=" + filedAsOfDate
                    #time.sleep(5)

            newExactFinalName = partsArr[0] + "_" + partsArr[1] + "__" + conformedYear + "_" + filedAsOfDate + "_" + partsArr[2]
            print "New exact file name=" + newExactFinalName
            newFinalName = exactFileNameArr[0] + "\\" + exactFileNameArr[1] + "\\" + exactFileNameArr[2] + "\\" + exactFileNameArr[3] + "\\" + exactFileNameArr[4] + "\\" + exactFileNameArr[5] + "\\" + newExactFinalName
            print "New file name=" + newFinalName
            os.rename(fileName, newFinalName)
            time.sleep(5)

if __name__ == '__main__':
    main()
