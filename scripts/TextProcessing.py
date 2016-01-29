__author__ = 'Anarchy'

def main():
    fileName = "D:/work/TUM/study/IDP/temp2.txt"
    lines = tuple(open(fileName, 'r'))
    for line in lines:
        line = line[:-1]
        #print line
        lineArr = line.split("\\")
        #print "length of array=" + str(len(lineArr))
        if ("trial" in line):
            tempName = lineArr[6]
            tempNameArr = tempName.split("_")
            print tempNameArr[1]
        else:
            print lineArr[3]

if __name__ == '__main__':
    main()