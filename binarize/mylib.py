# -*- coding: utf-8 -*-
import cv2
import cv2.cv as cv

# クラス定義
class Img:
    def __init__(self, img, name):
        self.img = img
        self.name = name

    # 画素値255(白)の領域をラベリングする
    def labeling(self):

        def search(img, img_copy, x, y, val, size):
            if inside(img, x, y, size) and get(img, y, x) == 255 and get(img, y, x) == 255:
                set(img_copy, x, y, val)
                search(img, img_copy, x, y-1, val, size) # 上
                search(img, img_copy, x, y+1, val, size) # 下
                search(img, img_copy, x-1, y, val, size) # 左
                search(img, img_copy, x+1, y, val, size) # 右
                # search(img, img_copy, x-1, y-1, val) # 左上
                # search(img, img_copy, x+1, y-1, val) # 右上
                # search(img, img_copy, x-1, y+1, val) # 左下
                # search(img, img_copy, x+1, y-1, val) # 右下

        def inside(img, x, y, size):
            x >= 0 and x < size[0] and y >=0 and y < size[1]
            
        def get(img, x, y):
            cv.Get2D(img, y, x)[0]
    
        def set(img, x, y, val):
            cv.Set2D(img, y, x, val)

        img = cv.fromarray(self.img)
        label_val = 0
        size = cv.GetSize(img) # return (width, height)
        img_copy = cv.CreateImage(size, 8, 1)
        #    img_copy = copy.deepcopy(img)

        # img_copyの初期化
        for y in xrange(size[1]):
            for x in xrange(size[0]):
                set(img_copy, x, y, 0)
             
        # ラベリング
        for y in range(size[1]):
            for x in range(size[0]):
                if get(img, y, x) == 255 and get(img_copy, y, x) == 255:
                    label_val += 1
                    search(img, img_copy, x, y, count, size)

        return img_copy
