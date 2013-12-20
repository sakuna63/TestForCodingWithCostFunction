import glob
import cv2

files = glob.glob("./img/*.bmp")

for f in files:
    img = cv2.imread(f, 0)
    name = f.split("/")[-1].replace(".bmp", "")
    cv2.imwrite("./img/" + name + ".png", img)
    
