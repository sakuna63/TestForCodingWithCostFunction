# -*- coding: utf-8 -*-
import cv2  
import shutil
import cv2.cv as cv
import os
import mylib as my
import sys
import glob
import math
import numpy as np
from matplotlib import pyplot as plt

# メインの処理
num_times = int(sys.argv[1])
# 切り上げ
num_div = math.ceil(255/num_times)

files = glob.glob("../TestForImageDegrationOfSteganography/img/origin/*.*")
#file = files[0]

imgs = []


for i, file in enumerate(files):
    try:  
        img = cv2.imread(file, 0) # 0 = GrayScale
    except:  
        print 'faild to load %s' % file
        quit()  

    name_f = file.split("/")
    imgss = []
    print name_f

    for i in xrange(num_times):
        
        if i == 0: # 0を含む領域を抽出するときだけ別処理
            # 上限以下を255に変換する
            ret, img_threshed = cv2.threshold(img, (i+1)*num_div-1, 255, cv2.THRESH_BINARY_INV) # 0 if src(x,y) > thresh
        else:
            # 欲しい画素値域の下限以下を0にする
            ret, img_threshed = cv2.threshold(img, i*num_div - 1, 255, cv2.THRESH_TOZERO) # 0 if src(x,y) <= thresh
            # 上限以上を0にする
            ret, img_threshed = cv2.threshold(img_threshed, (i+1)*num_div - 1, 255, cv2.THRESH_TOZERO_INV) # 0 if src(x,y) > thresh
            # 欲しい領域(0でない部分)を255に変換する
            ret, img_threshed = cv2.threshold(img_threshed, 0, 255, cv2.THRESH_BINARY) # maxval if src(x,y) > thresh
        
        imgss.append(my.Img(img_threshed, name_f[-1]))
    
    imgs.append(imgss)

dir = "../TestForImageDegrationOfSteganography/img/img_b/"


for img in imgs: 
    name = img[0].name.split("\\.")[-1]
    shutil.rmtree(dir + name + "/")
    os.mkdir(dir + name + "/")
    for i, im in enumerate(img):
        if(i <= 9) : 
            off = "0"
        else :
            off = ""
        cv2.imwrite(dir + name + "/" + off + str(i) + ".bmp", im.img)
