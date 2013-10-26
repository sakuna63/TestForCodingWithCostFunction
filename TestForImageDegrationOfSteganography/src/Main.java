import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
	private static final String CSV_FILE_PATH = "./csv/";
	private static final String CHARACTER_CODE = "US-ASCII";
	
	// CHARACTER_CODEのサイズ
	private static final int CHARACTER_SIZE = 8;
	// 画像の一辺のサイズ
	private static final int IMAGE_SIZE = 256;
	
	private static final int[] ERROR_CODE_LENGTHS = new int[]{
		8, 16, 24, 32, 40, 48, 56, 64, 72, 80, 88, 96, 
		104, 112, 120, 128//, 136, 144, 152, 160, 168, 176,
//		184, 192, 200, 208, 216, 224, 232, 240, 248, 256
	};
	
	public static void main(String[] args) {
		int messageLenght;
		int[] msg;
		File imgDir = new File(IMAGE_PATH);
		PrintWriter pw;
		
		for(File f : imgDir.listFiles()) {
//			for(int seed=0; seed<=32; seed++) {
				pw = getPrintWriter(CSV_FILE_PATH + f.getName().replace(".bmp", "") + ".csv" );
				pw.println("誤りパターン長,埋め込み率,PSNR,誤り率");		
				
				msg = getRandomTextByte(0, IMAGE_SIZE * IMAGE_SIZE / 8);
//				msg = getRandomTextByte(seed, IMAGE_SIZE * IMAGE_SIZE / 8);
//				calcEntropy(msg, (int) Math.pow(2, CHARACTER_SIZE));
				
				for(int codeLength : ERROR_CODE_LENGTHS) {
					messageLenght = IMAGE_SIZE * IMAGE_SIZE / codeLength;
					execStegoProcess(f, pw, msg, messageLenght, codeLength);
				}
				
				pw.close();
//			}
		}
		
		Util.print("埋め込み終了");
    }

	/**
	 * ステゴデータの生成プロセスを実行する
	 * @param file
	 * @param pw
	 * @param msg
	 * @param table
	 * @param msgLength
	 * @param codeLength
	 */
	private static void execStegoProcess(File file, PrintWriter pw, int[] msg, int msgLength, int codeLength) {
		int offset = 0;
		byte[] sBuff = null, cBuff = null,
				sizeBuff = new byte[4], offsetBuff = new byte[4];
		
		/** 画像の読み込み **/
		try {
			// Streamクラスの初期化
			FileInputStream fis = new FileInputStream(file);
			
			// 画像のサイズを読み込む
			fis.skip(2);
			fis.read(sizeBuff);
			int size = sizeBuff[3] << 24 | sizeBuff[2] << 16 | sizeBuff[1] << 8 | sizeBuff[0];
			// 画像のオフセットを読み込む
			fis.skip(4);
			fis.read(offsetBuff);
			offset = offsetBuff[3] << 24 | offsetBuff[2] << 16 | offsetBuff[1] << 8 | offsetBuff[0];
			
			// バッファにビットマップを読み込む
			sBuff = new byte[size];
			
			fis.close();
			fis = new FileInputStream(file);
			
			fis.read(sBuff);
			fis.close();
			
			cBuff = sBuff.clone();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if( sBuff == null || cBuff == null ) {
			Util.print("画像の読み込みに失敗しました。");
			return;
		}
		
		/** メッセージの埋め込み **/
		long errorRate = embeding(sBuff, msg, msgLength, codeLength, offset);
		outputImg(file, sBuff, codeLength);

		double psnr = Calc.PSNR(sBuff, cBuff, offset);
		pw.println(codeLength + "," + ((double)8/codeLength) * 100 + "," + psnr + "," + (double)errorRate/(msgLength * codeLength) * 100);
		
		int[] eMsg = extracting(sBuff, cBuff, offset, msgLength, codeLength);
		
		if( !compMsg(msg, eMsg, codeLength) )
			Util.println("メッセージの取り出しに失敗しました");

	}
	
	/**
	 * 誤りパターンをimgに埋め込む(1pix = 8bit),同時に誤り率（平均）を計算して返す
	 * @param img
	 * @param msg
	 * @param table
	 * @param n
	 */
	private static long embeding(byte[] img, int[] msg, int msgLength, int codeLength, int offset) {
		int[] ep;
		byte[] eppArray;
        int baseIndex = offset;
        long errorRate = 0;
        
        if( msgLength * codeLength > img.length - offset) {
        	Util.println("メッセージが長過ぎます");
        	return -1;
        }
        
        // メッセージの数だけ繰り返す
        for(int i=0; i<msgLength; i++) {
        	// メッセージに対応する誤りパターンを取り出す
        	ep = Util.message2Error(msg[i], codeLength);
        	for(int e : ep)
        		errorRate += Util.hamingWeight(e);
        	eppArray = Util.extractErrorPutternPerPix(ep, codeLength);
        	// １ピクセルでARGBの32ビットなのでそれぞれのLSBに対し、誤りパターンのMSBから順に排他的論理和をとる
        	for(int j=0; j<codeLength ; j++) {
        		img[baseIndex + j] = (byte) (img[baseIndex + j] ^ eppArray[codeLength-j-1]);
        	}
        	baseIndex += codeLength;
        }
        
        return errorRate;
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
		
		for(int i=offset; i+codeLength <= stego.length; i+=codeLength) {
			ep = Util.extractErrorPattern(stego, cover, i, codeLength);
			// 誤りパターンから埋め込みデータを復元する
			if( codeLength >= 136 && i == 41)
				Util.print(1);
			msg[(i-offset)/codeLength] = Util.error2Message(codeLength, ep);
		}
		
		return msg;
	}
	
	private static boolean compBuff(byte[] sBuff, byte[] cBuff, int offset) {
		boolean flag = false;
		
		for(int i=offset; i<sBuff.length; i++) {
//			Util.println(String.format("i: %d, s:%d, c:%d",i, sBuff[i], cBuff[i]));
			if( sBuff[i] != cBuff[i]) {
//				Util.println(String.format("i: %d, s:%d, c:%d",i, sBuff[i], cBuff[i]));
				flag = true;
				break;
			}
		}
		return flag;
		
	}
	
	/**
	 * ２つのメッセージを比較する
	 * @param msg1
	 * @param msg2
	 * @return
	 */
	private static boolean compMsg(int[] origin, int[] embuded, int codeLength) {
		boolean flag = true;
		
		for(int i=0; i<embuded.length; i++) {
			if( origin[i] != embuded[i]) {
				Util.println(String.format("length:%d, i: %d, origin:%d, embuded:%d",codeLength, i, origin[i], embuded[i]));
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
	private static int[] getRandomTextByte(int seed, int textNum) {
		Sfmt s = new Sfmt(seed);
		int[] randByteArray = new int[textNum];
	
		for( int i=0; i<textNum; i++)
		    randByteArray[i] = s.NextInt((int) Math.pow(2, CHARACTER_SIZE));
		
		return randByteArray;
    }

	
	/**
	 * sBuff(bmpのバイナリ)を出力する
	 * @param file
	 * @param sBuff
	 * @param codeLength
	 */
	private static String outputImg(File file, byte[] sBuff, int codeLength) {
		// 画像を出力する
		FileOutputStream output = null;
		String name = BURIED_IMAGE_PATH + codeLength + file.getName();
		File stegoFile = new File(name);
		
		if( stegoFile.exists() ) 
			stegoFile.delete();
		
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
		
		return name;
	}
	
	/**
	 * PrinterWriterのインスタンスを取得する
	 * @param fileName
	 * @return
	 */
	
	private static PrintWriter getPrintWriter(String fileName) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName)),"Shift_JIS"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pw;
	}

	private static void calcEntropy(int[] msg, int max) {
		int[] count = new int[max];
		double p;
		
		for(int i=0; i<msg.length; i++) {
			count[msg[i]]++;
		}
		
		
		double dispersion = 0;
		for(int j=0; j<max; j++) {
			p = (double)count[j]/msg.length;
			dispersion -= p * Calc.log2(p);
		}
		
		Util.println("エントロピー：" + dispersion);
	}
	
}
