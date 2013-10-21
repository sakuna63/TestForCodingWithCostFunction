import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import my.util.Calc;
import my.util.Util;

/**
 * 前提条件
 * ・US-ASCII
 * ・GrayScale
 * ・256 * 256
 * ・WindowsフォーマットのBitmap
 * @author sakuna63
 */

public class Main {
	private static final String IMAGE_PATH = "./img/";
	private static final String BURIED_IMAGE_PATH = "./embuded_img/";
	private static final String CHARACTER_CODE = "US-ASCII";
	
	// CHARACTER_CODEのサイズ
	private static final int CODE_SIZE = 8;
	// 画像の一辺のサイズ
	private static final int IMAGE_SIZE = 256;
	
	private static final int[] ERROR_CODE_LENGTHS = new int[]{
		8, 16, 32, 64, 128//, 256
	};
	
	public static void main(String[] args) {
		int messageLenght;
		int[] msg;
		int[][] table;
		for(int length : ERROR_CODE_LENGTHS) {
			messageLenght = IMAGE_SIZE * IMAGE_SIZE / length;
			msg = getRandomTextByte(messageLenght);
			table = Util.errorPatternTable(length, (int) Math.pow(2, CODE_SIZE));
			
			File imgDir = new File(IMAGE_PATH);
			for(File f : imgDir.listFiles()) {
				execStegoProcess(f, msg, table, messageLenght, length);
			}
			Util.print(length);
		}
    }
	
	@SuppressWarnings("resource")
	private static void execStegoProcess(File f, int[] msg, int[][] table, int length, int n) {
		FileInputStream stego = null;
		FileInputStream cover = null;
		FileOutputStream output = null;
		File oFile = new File(BURIED_IMAGE_PATH + n + f.getName());
		
		int size = 0, offset = 0;
		byte[] sBuff = null, cBuff = null, 
			   sizeBuff = new byte[4], offsetBuff = new byte[4];
		try {
			// 出力用のファイルを生成
			oFile.createNewFile();
			// Streamクラスの初期化
			stego = new FileInputStream(f);
			cover = new FileInputStream(f);
			output = new FileOutputStream(oFile);
			// Bitmapのサイズを読み込む
			stego.skip(2);
			stego.read(sizeBuff);
			stego.skip(4);
			stego.read(offsetBuff);
			stego = new FileInputStream(f);
			size = sizeBuff[3] << 24 | sizeBuff[2] << 16 | sizeBuff[1] << 8 | sizeBuff[0];
			offset = offsetBuff[3] << 24 | offsetBuff[2] << 16 | offsetBuff[1] << 8 | offsetBuff[0];
			// バッファにビットマップを読み込む
			sBuff = new byte[size];
			cBuff = new byte[size];
			stego.read(sBuff);
			cover.read(cBuff);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if( sBuff == null || cBuff == null ) {
			Util.print("画像の読み込みに失敗しました。");
			return;
		}
		
		embeding(sBuff, msg, table, n, offset);

//		Util.println("PSNR:"+Calc.PSNR(cBuff, cBuff, offset));
		Util.println("PSNR:"+Calc.PSNR(sBuff, cBuff, offset));
		
		int[] msg2 = extracting(sBuff, cBuff, offset, n, length);
		Util.println( compMsg(msg, msg2) ? "メッセージの取り出しに成功しました" : "メッセージの取り出しに失敗しました");

		try {
			output.write(sBuff);
			stego.close();
			cover.close();
			output.close();
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
	 private static void embeding(byte[] img, int[] msg, int[][] table, int n, int offset) {
        byte[] eppArray;
        int baseIndex = offset;
        
        if( msg.length * n > img.length - offset) {
        	Util.println("メッセージが長過ぎます");
        	return;
        }
        
        // メッセージの数だけ繰り返す
        for(int m : msg) {
        	// メッセージに対応する誤りパターンを取り出す
//        	Util.print("byte:%x int:%d", m, (int)(m & 0xFF));
        	eppArray = Util.extractErrorPutternPerPix(table[m], n);
        	// １ピクセルでARGBの32ビットなのでそれぞれのLSBに対し、誤りパターンのMSBから順に排他的論理和をとる
        	for(int i=0; i<n ; i++) {
        		img[baseIndex + i] = (byte) (img[baseIndex + i] ^ eppArray[n-i-1]);
        	}
        	baseIndex += n;
        }
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
	private static int[] extracting(byte[] stego, byte[] cover, int offset, int n, int length) {
		int[] msg = new int[length];
		int[] ep = new int[8];
		
		for(int i=offset; i<stego.length; i+=n) {
			ep = Util.extractErrorPattern(stego, cover, i, n);
			// 誤りパターンから埋め込みデータを復元する
			msg[(i-offset)/n] = Util.error2Message(n, ep);
		}
		
		return msg;
	}
	
	/**
	 * ２つのメッセージを比較する
	 * @param msg1
	 * @param msg2
	 * @return
	 */
	private static boolean compMsg(int[] msg1, int[] msg2) {
		boolean flag = true;
		if(msg1.length != msg2.length) return false;
		
		for(int i=0; i<msg1.length; i++) {
			if( msg1[i] != msg2[i]) {
				Util.println(String.format("i: %d, msg1:%d, msg2:%d",i, msg1[i], msg2[i]));
				flag = false;
//				break;
			}
		}
		return flag;
	}

	
	/**
	 * 乱数列を生成する
	 * @param textNum
	 * @return
	 */
	private static int[] getRandomTextByte(int textNum) {
		Sfmt s = new Sfmt(1);
		int[] randByteArray = new int[textNum];
	
		for( int i=0; i<textNum; i++)
		    randByteArray[i] = s.NextInt((int) Math.pow(2, CODE_SIZE));
		
		return randByteArray;
    }
}
