import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.imageio.ImageIO;

import my.util.Calc;
import my.util.Util;

/**
 * US-ASCIIを用いることを前提にコードを書く
 * @author sakuna63
 */

public class Main {
	private static final String IMAGE_PATH = "./img/";
	private static final String BURIED_IMAGE_PATH = "./buried_img/";
	private static final String CHARACTER_CODE = "US-ASCII";
	
	// CHARACTER_CODEのサイズ
	private static final int CODE_SIZE = 8;
	// エラーパターン長
	private static final int ERROR_PATTERN_LENGTH = 8;
	// １ピクセルに対して埋め込むビットサイズ
	private static final int ERROR_PER_PIX = 1;//4;
	// メッセージ長
	private static final int MESSAGE_LENGTH = 256 * 256 * ERROR_PER_PIX / ERROR_PATTERN_LENGTH;
	
	public static void main(String[] args) {
		byte[] msg = getRandomTextByte(MESSAGE_LENGTH);
		int[] table = Calc.errorPatternTable(ERROR_PATTERN_LENGTH, (int) Math.pow(2, CODE_SIZE));
		
		File imgDir = new File(IMAGE_PATH);
		BufferedImage img1 = null;
		BufferedImage img2 = null;
		try {
			img1 =  ImageIO.read(imgDir.listFiles()[0]);
			img2 =  ImageIO.read(imgDir.listFiles()[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if( img1 != null ) {
			img1 = buryErrorPattern2(img1, msg, table, ERROR_PATTERN_LENGTH);
		} else {
			Util.print("画像の読み込みに失敗しました。");
		}

		Util.print(Calc.PSNR(img1, img2));
		
		try {
			ImageIO.write(img1, "bmp", new File(BURIED_IMAGE_PATH + "buried.bmp"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	/**
	 * 誤りパターンが埋め込まれたimgを返す(1pix = 32bit)
	 * @param img
	 * @param msg
	 * @param table
	 * @param n
	 */
	private static BufferedImage buryErrorPattern1(BufferedImage img, byte[] msg, int[] table, int n) {
		int argb[];
		int imgW = img.getWidth(), imgH = img.getHeight();
		int imgX = 0, imgY = 0;
		
		int[] errorPattern;
		
		// メッセージの数だけ繰り返す
		for(byte m : msg) {
			// メッセージに対応する誤りパターンを取り出す
			errorPattern = Util.putErrorPuttern(table[(int)(m & 0xFF)], n);
			// １ピクセルでARGBの32ビットなのでそれぞれのLSBに対し、誤りパターンのMSBから順に排他的論理和をとる
			for(int i=n-1; 0<=i; i-=4) {
				argb = Util.putARGB(img.getRGB(imgX, imgY));
				argb[0] = argb[0] ^ errorPattern[i];
				argb[1] = argb[1] ^ errorPattern[i-1];
				argb[2] = argb[2] ^ errorPattern[i-2];
				argb[3] = argb[3] ^ errorPattern[i-3];
				
				img.setRGB(imgX, imgY, argb[0] << 24 | argb[1] << 16 | argb[2] << 8 | argb[3]);
				
				imgX++;
				if(imgX >= imgW) {
					imgX = 0;
					imgY++;
					if( imgY >= imgH ) {
						Util.print("max size");
						return img;
					}
				}
			}
		}
		return img;
	}

	/**
	 * 誤りパターンを埋め込まれたimgを返す(1pix = 8bit)
	 * @param img
	 * @param msg
	 * @param table
	 * @param n
	 * @return
	 */
	private static BufferedImage buryErrorPattern2(BufferedImage img, byte[] msg, int[] table, int n) {
		int argb[];
		int imgW = img.getWidth(), imgH = img.getHeight();
		int imgX = 0, imgY = 0;
		
		int[] errorPattern;
		
		// メッセージの数だけ繰り返す
		for(byte m : msg) {
			// メッセージに対応する誤りパターンを取り出す
			errorPattern = Util.putErrorPuttern(table[(int)(m & 0xFF)], n);
			// １ピクセルでARGBの32ビットなのでそれぞれのLSBに対し、誤りパターンのMSBから順に排他的論理和をとる
			for(int i=n-1; 0<=i; i--) {
				argb = Util.putARGB(img.getRGB(imgX, imgY));
				argb[1] = argb[1] ^ errorPattern[i];
				argb[2] = argb[2] ^ errorPattern[i];
				argb[3] = argb[3] ^ errorPattern[i];
				
				img.setRGB(imgX, imgY, argb[0] << 24 | argb[1] << 16 | argb[2] << 8 | argb[3]);
				
				imgX++;
				if(imgX >= imgW) {
					imgX = 0;
					imgY++;
					if( imgY >= imgH ) {
						Util.print("max size");
						return img;
					}
				}
			}
		}
		return img;
	}

	private static byte[] putBuriedMessage(BufferedImage img, byte[] msg, int[] table, int n) {
		
		return null;
	}

	/**
	 * ランダムな文字列を発生させる
	 * @param textNum
	 * @return
	 */
	private static String getRandomText(int textNum) {
		byte[] randByteArray = getRandomTextByte(textNum);
		String text = null;
		try {
			text =  new String(randByteArray, CHARACTER_CODE);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return text;
    }

	/**
	 * 乱数列を生成する
	 * @param textNum
	 * @return
	 */
	private static byte[] getRandomTextByte(int textNum) {
		Sfmt s = new Sfmt(1);
		Integer num;
		byte byte_num;
		byte[] randByteArray = new byte[textNum];
	
		for( int i=0; i<textNum; i++) {
		    num = s.NextInt((int) Math.pow(2, CODE_SIZE));
		    byte_num = num.byteValue();
		    randByteArray[i] = byte_num;
		}
		return randByteArray;
    }
}
