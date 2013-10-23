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
	private static final String CSV_FILE_PATH = "./csv/";
	private static final String CHARACTER_CODE = "US-ASCII";
	
	// CHARACTER_CODEのサイズ
	private static final int CHARACTER_SIZE = 8;
	// 画像の一辺のサイズ
	private static final int IMAGE_SIZE = 256;
	
	private static final int[] ERROR_CODE_LENGTHS = new int[]{
		8, 16, 32, 64, 128//, 256
	};
	
	public static void main(String[] args) {
		int messageLenght;
		int[] msg;
		File imgDir = new File(IMAGE_PATH);
		PrintWriter pw;
		
		for(File f : imgDir.listFiles()) {
			pw = getPrintWriter(CSV_FILE_PATH + f.getName() + ".csv" );
			outputCsvHead(pw, f.getName(), ERROR_CODE_LENGTHS);
			
			for(int seed=0; seed<=32; seed++) {
				pw.print(CHARACTER_CODE + "," + seed + ",");
				
				msg = getRandomTextByte(seed, IMAGE_SIZE * IMAGE_SIZE / 8);
				calcEntropy(msg, (int) Math.pow(2, CHARACTER_SIZE));
				
				for(int codeLength : ERROR_CODE_LENGTHS) {
					messageLenght = IMAGE_SIZE * IMAGE_SIZE / codeLength;
					execStegoProcess(f, pw, msg, messageLenght, codeLength);
				}
				pw.println();
			}
			pw.close();
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
	@SuppressWarnings("resource")
	private static void execStegoProcess(File file, PrintWriter pw, int[] msg, int msgLength, int codeLength) {
		int offset = 0;
		byte[] sBuff = null, cBuff = null,
				sizeBuff = new byte[4], offsetBuff = new byte[4];
		
		/** 画像の読み込み **/
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
		
		/** メッセージの埋め込み **/
		embeding(sBuff, msg, msgLength, codeLength, offset);

		double psnr = Calc.PSNR(sBuff, cBuff, offset);
//		Util.println(psnr);
		pw.print(psnr + ","	);
		
		int[] eMsg = extracting(sBuff, cBuff, offset, msgLength, codeLength);
		
		if( !compMsg(msg, eMsg) )
			Util.println("メッセージの取り出しに失敗しました");
		
		outputImg(file, sBuff, codeLength);
	}
	
	/**
	 * 誤りパターンをimgに埋め込む(1pix = 8bit)
	 * @param img
	 * @param msg
	 * @param table
	 * @param n
	 */
	private static void embeding(byte[] img, int[] msg, int msgLength, int codeLength, int offset) {
		int[] ep;
		byte[] eppArray;
        int baseIndex = offset;
        
        if( msgLength * codeLength > img.length - offset) {
        	Util.println("メッセージが長過ぎます");
        	return;
        }
        
        // メッセージの数だけ繰り返す
        for(int i=0; i<msgLength; i++) {
        	// メッセージに対応する誤りパターンを取り出す
        	ep = Util.message2Error(msg[i], codeLength);
        	eppArray = Util.extractErrorPutternPerPix(ep, codeLength);
        	// １ピクセルでARGBの32ビットなのでそれぞれのLSBに対し、誤りパターンのMSBから順に排他的論理和をとる
        	for(int j=0; j<codeLength ; j++) {
        		img[baseIndex + j] = (byte) (img[baseIndex + j] ^ eppArray[codeLength-j-1]);
        	}
        	baseIndex += codeLength;
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
	private static boolean compMsg(int[] origin, int[] embuded) {
		boolean flag = true;
		
		for(int i=0; i<embuded.length; i++) {
			if( origin[i] != embuded[i]) {
				Util.println(String.format("i: %d, origin:%d, embuded:%d",i, origin[i], embuded[i]));
				flag = false;
				break;
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
	
	/**
	 * csvの1行目を出力する
	 * @param pw
	 * @param fileName
	 * @param errorCodeLengths
	 */
	private static void outputCsvHead(PrintWriter pw, String fileName, int[] errorCodeLengths) {
		pw.print(fileName + ",,");
		for( int length : errorCodeLengths ) {
			pw.print(length + ",");
		}
		pw.println();
	}
	
	/**
	 * PrinterWriterのインスタンスを取得する
	 * @param fileName
	 * @return
	 */
	
	private static PrintWriter getPrintWriter(String fileName) {
		PrintWriter pw = null;
		try {
			FileWriter fw = new FileWriter(fileName);
			pw = new PrintWriter(new BufferedWriter(fw));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pw;
	}
	
	
	private static void calcDispersion(int[] msg, int max) {
		int[] count = new int[max];
		int ave = msg.length / max;
		for(int i=0; i<msg.length; i++) {
			count[msg[i]]++;
		}
		
		double dispersion = 0;
		for(int j=0; j<max; j++) {
			dispersion += Math.pow(ave - count[j], 2);
		}
		dispersion /= ave;
		
		Util.println("分散：" + dispersion);
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
