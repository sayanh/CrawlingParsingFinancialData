import shutil

def main():
        lines = tuple(open("D:/work/TUM/study/IDP/Stringoutofbounds22022015.txt", 'r'))
        for line in lines:
            line = line[:-1]
            print line
            shutil.copy2(line, "D:\work\TUM\study\IDP\\trial")

        #shutil.copy2("E:/IDP/Extracted Files/1506814/16054_1506814_raw.txt", "D:/work/TUM/study/IDP/trial")

if __name__ == '__main__':
    main()