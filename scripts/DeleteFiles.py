__author__ = 'Anarchy'
import os

def main():
    source = "E:\IDP\ParseFiles"
    destFile = "D:\work\TUM\study\IDP\FilesTobeDeleted.txt"
    fwrite = open(destFile, 'w')
    for  root, dirs, files in os.walk(source):
        for file in files:
            fileArr = file.split('_')
            if ("item1" in file or "item2" in file):
                if (len(fileArr) < 5):
                    file = root + "\\" + file
                    print file
                    fwrite.write(file+"\n")
                    os.remove(file)
            else:
                if (len(fileArr) < 4):
                    file = root + "\\" + file
                    print file
                    fwrite.write(file+"\n")
                    os.remove(file)
            fwrite.flush()
    fwrite.close()




if __name__ == '__main__':
    main()