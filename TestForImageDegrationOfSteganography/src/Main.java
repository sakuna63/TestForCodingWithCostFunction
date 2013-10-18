import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

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
	// メッセージ長
	private static final int MESSAGE_LENGTH = 256 * 256 / ERROR_PATTERN_LENGTH;
	
	public static void main(String[] args) {
		byte[] msg = getRandomTextByte(MESSAGE_LENGTH);
		int[] table = Calc.errorPatternTable(ERROR_PATTERN_LENGTH, (int) Math.pow(2, CODE_SIZE));
		HashMap<Integer, Integer> antiTable = Util.aitiTable(table);
		
		File imgDir = new File(IMAGE_PATH);
		for(File f : imgDir.listFiles()) {
			execStegoProcess(f, msg, table, antiTable, MESSAGE_LENGTH, ERROR_PATTERN_LENGTH);
		}
    }
	
	private static void execStegoProcess(File f, byte[] msg, int[] table, HashMap<Integer, Integer> anti, int length, int n) {
		BufferedImage stego = null;
		BufferedImage cover = null;
		try {
			stego =  ImageIO.read(f);
			cover =  ImageIO.read(f);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if( stego != null || cover != null ) {
			stego = embeding(stego, msg, table, n);
		} else {
			Util.print(f.getName() + "の読み込みに失敗しました。");
			return;
		}

		Util.print(Calc.PSNR(stego, cover));
		
//		byte[] msg2 = extracting(stego, cover, table, anti, n, length);
//		Util.print( compMsg(msg, msg2) ? "メッセージの取り出しに成功しました" : "メッセージの取り出しに失敗しました");
		
		try {
			ImageIO.write(stego, "bmp", new File(BURIED_IMAGE_PATH + f.getName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 誤りパターンをimgに埋め込む(1pix = 8bit)
	 * @param img
	 * @param msg
	 * @param table
	 * @param n
	 */
	private static BufferedImage embeding(BufferedImage img, byte[] msg, int[] table, int n) {
		int argb, index = 0;
		int imgW = img.getWidth(), imgH = img.getHeight();
		int[] eppArray = null;
		
		for(int i=0; i<imgH; i++) {
			for(int j=0; j<imgW; j++) {
				if(j % n == 0) {
					eppArray = Util.extractErrorPutternPerPix(table[(int)(msg[index] & 0xFF)], n);
					index++;
				}
				int rgb = img.getRGB(j, i);
				argb = Util.embededPix(rgb, eppArray[j % n]);
				Util.print(String.format("(%d,%d) %d -> %d", i,j,rgb,argb));
				img.setRGB(j, i, argb);
			}
		}
		return img;
	}
	
	/**
	 * ステゴデータから埋め込みデータを取り出す
	 * @param embedImg
	 * @param originImg
	 * @param table
	 * @param anti
	 * @param n: 誤りパターン長
	 * @param length: メッセージ長
	 * @return
	 */
	private static byte[] extracting(BufferedImage stego, BufferedImage cover, int[] table, HashMap<Integer, Integer> anti, int n, int length) {
		int argb1, argb2, ep = 0, index = 0;
		int imgW = stego.getWidth(), imgH = stego.getHeight();
		
		byte[] msg = new byte[length];
		
		for(int i=0; i<imgH; i++) {
			for(int j=0; j<imgW; j++) {
				// それぞれのピクセルからLSBのみを取り出す
				argb1 = stego.getRGB(j, i) & 0x00000001;
				argb2 = cover.getRGB(j, i) & 0x00000001;
				
				// とりだしたビットのXORをepのLSBに格納し1ビット左シフトする
				ep = (ep << 1) | (argb1 ^ argb2) ;
				
				// エラーパターン長と等しくなったら埋め込みデータを取り出す
				if( j % n == 0 ) {
					msg[index] = anti.get(ep).byteValue();
					index++;
					ep = 0;
				}
			}
		}
		
		return msg;
	}
	
	/**
	 * ２つのメッセージを比較する
	 * @param msg1
	 * @param msg2
	 * @return
	 */
	private static boolean compMsg(byte[] msg1, byte[] msg2) {
		boolean flag = true;
		if(msg1.length != msg2.length) return false;
		
		for(int i=0; i<msg1.length; i++) {
			if( msg1[i] != msg2[i]) {
				flag = false;
				break;
			}
		}
		return flag;
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
		byte[] randByteArray = new byte[textNum];
	
		for( int i=0; i<textNum; i++) {
		    num = s.NextInt((int) Math.pow(2, CODE_SIZE));
		    randByteArray[i] = num.byteValue();
		}
		return randByteArray;
    }
}
