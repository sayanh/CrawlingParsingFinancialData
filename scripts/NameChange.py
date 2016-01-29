__author__ = 'Anarchy'

def main():
    fileOpen = "D:/work/TUM/study/IDP/parsedfilenames.txt"
    fileOutput = "D:/work/TUM/study/IDP/parsedfilenames_output.txt"
    fwrite = open(fileOutput, 'w')
    lines = tuple(open(fileOpen, 'r'))
    count = 0
    for line in lines:
        line = line[0:len(line)-1]
        linearr = line.split('\\')
        length = len(linearr)

        if length!=5:
            count+=1
            print line
            print len(linearr)
            print str(linearr[6])
            nameFile = str(linearr[6])
            nameFileArr = nameFile.split('_')
            cik = nameFileArr[1]
            finalName = "E:\IDP\Downloaded\\10K\\" + cik + "\\" + nameFile + "\n"
            print finalName
            fwrite.write(finalName)
        else:

            finalName = "E:\IDP\Downloaded\\10K\\" + linearr[3] + "\\" + linearr[4] + "\n"
            print "final name normal=" + finalName
            fwrite.write(finalName)


    print "total =" + str(count)
        #print line

if __name__ == '__main__':
    main()