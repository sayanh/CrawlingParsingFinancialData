import os
from os import listdir

def main():
    temp = "2015-01-11 01:25:25,725 ERROR [pool-223-thread-1] root: Error is main fileParseCore for file E:\IDP\Extracted Files\\16732\\2663_16732_159.txt  |java.nio.charset.MalformedInputException: Input length = 1"
    tempArr = temp.split("file ")
    tempArr1 = tempArr[1].split(" |")
    print tempArr1[0]



if __name__ == '__main__':
    main()



