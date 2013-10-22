import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
	private static final String CSV_FILE_NAME = "./data.csv";
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
		PrintWriter pw = getPrintWriter(CSV_FILE_NAME);
		
		for(int codeLength : ERROR_CODE_LENGTHS) {
			messageLenght = IMAGE_SIZE * IMAGE_SIZE / codeLength;
			msg = getRandomTextByte(messageLenght);
			table = Util.errorPatternTable(codeLength, (int) Math.pow(2, CODE_SIZE));
			
			File imgDir = new File(IMAGE_PATH);
			for(File f : imgDir.listFiles()) {
				execStegoProcess(f, pw, msg, table, messageLenght, CHARACTER_CODE, codeLength);
			}
		}
		
		pw.close();
    }
	
	@SuppressWarnings("resource")
	private static void execStegoProcess(File file, PrintWriter pw, int[] msg, int[][] table, int msgLength, String codeName, int codeLength) {
		int offset = 0;
		byte[] sBuff = null, cBuff = null,
				sizeBuff = new byte[4], offsetBuff = new byte[4];
		
		try {
			// Streamクラスの初期化
			FileInputStream stego = new FileInputStream(file);
			FileInputStream cover = new FileInputStream(file);

			// 画像のサイズを読み込む
			stego.skip(2);
			stego.read(sizeBuff);
			int size = sizeBuff[3] << 24 | sizeBuff[2] << 16 | sizeBuff[1] << 8 | sizeBuff[0];
			// 画像のオフセットを読み込む
			stego.skip(4);
			stego.read(offsetBuff);
			offset = offsetBuff[3] << 24 | offsetBuff[2] << 16 | offsetBuff[1] << 8 | offsetBuff[0];
			
			// reset非対応なのでnewで読み込み位置を初期化
			stego = new FileInputStream(file);
			
			// バッファにビットマップを読み込む
			sBuff = new byte[size];
			cBuff = new byte[size];
			stego.read(sBuff);
			cover.read(cBuff);
			stego.close();
			cover.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if( sBuff == null || cBuff == null ) {
			Util.print("画像の読み込みに失敗しました。");
			return;
		}
		
		embeding(sBuff, msg, table, codeLength, offset);

		int[] msg2 = extracting(sBuff, cBuff, offset, msgLength, codeLength);
		Util.println( compMsg(msg, msg2) ? "メッセージの取り出しに成功しました" : "メッセージの取り出しに失敗しました");
		
		outputImg(file, sBuff, codeLength);
		outputCsv(pw, file.getName(), codeName, codeLength, Calc.PSNR(sBuff, cBuff, offset));
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
	private static int[] extracting(byte[] stego, byte[] cover, int offset, int msgLength, int codeLength) {
		int[] msg = new int[msgLength];
		int[] ep = new int[8];
		
		for(int i=offset; i<stego.length; i+=codeLength) {
			ep = Util.extractErrorPattern(stego, cover, i, codeLength);
			// 誤りパターンから埋め込みデータを復元する
			msg[(i-offset)/codeLength] = Util.error2Message(codeLength, ep);
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

	private static void outputImg(File file, byte[] sBuff, int codeLength) {
		// 画像を出力する
		FileOutputStream output = null;
		File stegoFile = new File(BURIED_IMAGE_PATH + codeLength + file.getName());
		
		try {
			stegoFile.createNewFile();
			output = new FileOutputStream(stegoFile);
			output.write(sBuff);
			output.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static PrintWriter getPrintWriter(String fileName) {
		PrintWriter pw = null;
		try {
			FileWriter fw = new FileWriter("./data.csv");
			pw = new PrintWriter(new BufferedWriter(fw));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pw;
	}
	
	private static void outputCsv(PrintWriter pw, String fileName, String codeName, int codeLength, double psnr) {
		// csvにデータを出力する
		pw.print(fileName);
		pw.print(",");
		pw.print(codeName);
		pw.print(",");
		pw.print(codeLength);
		pw.print(",");
		pw.print(psnr);
		pw.println();
	}
}
