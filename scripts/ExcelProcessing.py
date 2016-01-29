__author__ = 'Anarchy'
import time


def main():
    csvFileName = "D:/work/TUM/study/IDP/AllData.csv"
    outputCSVFile = "D:/work/TUM/study/IDP/AllData_output.csv"
    sourceFile = ""
    foutput = open(outputCSVFile, 'w')
    lines = tuple (open(csvFileName, 'r'))
    linesCiks = tuple(open("D:/work/TUM/study/IDP/temp1.txt"))
    targetText = ""
    for line in lines:
        #print "processing line=" + line
        for lineCik in linesCiks:
            lineCik = lineCik[:-1]
            if (lineCik in line):
                lineCikArr = line.split(',')
                print lineCikArr[3]
                '''if (lineCikArr[4]==""):
                    fileParsedNameArr = lineCikArr[1].split('_')
                    lineCikArr[4]=fileParsedNameArr[0]
                    print line
                    time.sleep(5)
                '''
    foutput.close()
    print "finished processing " + str(len(lines)) + " lines"


if __name__ == '__main__':
    main()