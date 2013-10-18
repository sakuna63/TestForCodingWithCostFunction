import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

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
	// エラーパターン長
	private static final int ERROR_PATTERN_LENGTH = 8;
	// メッセージ長
	private static final int MESSAGE_LENGTH = 256 * 256 / ERROR_PATTERN_LENGTH;
	
	public static void main(String[] args) {
		byte[] msg = getRandomTextByte(MESSAGE_LENGTH);
		int[] table = Util.errorPatternTable(ERROR_PATTERN_LENGTH, (int) Math.pow(2, CODE_SIZE));
		HashMap<Integer, Integer> antiTable = Util.antiTable(table);
		
		File imgDir = new File(IMAGE_PATH);
		for(File f : imgDir.listFiles()) {
			execStegoProcess(f, msg, table, antiTable, MESSAGE_LENGTH, ERROR_PATTERN_LENGTH);
		}
    }
	
	@SuppressWarnings("resource")
	private static void execStegoProcess(File f, byte[] msg, int[] table, HashMap<Integer, Integer> anti, int length, int n) {
		FileInputStream stego = null;
		FileInputStream cover = null;
		FileOutputStream output = null;
		File oFile = new File(BURIED_IMAGE_PATH + f.getName());
		
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
		
		byte[] msg2 = extracting(sBuff, cBuff, offset, table, anti, n, length);
		Util.println( compMsg(msg, msg2) ? "メッセージの取り出しに成功しました" : "メッセージの取り出しに失敗しました");

		try {
			output.write(sBuff);
			stego.close();
			cover.close();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Util.println("end");
	}
	
	/**
	 * 誤りパターンをimgに埋め込む(1pix = 8bit)
	 * @param img
	 * @param msg
	 * @param table
	 * @param n
	 */
	 private static void embeding(byte[] img, byte[] msg, int[] table, int n, int offset) {
        byte[] eppArray;
        int baseIndex = offset;
        
        if( msg.length * n > img.length - offset) {
        	Util.println("メッセージが長過ぎます");
        	return;
        }
        
        // メッセージの数だけ繰り返す
        for(byte m : msg) {
        	// メッセージに対応する誤りパターンを取り出す
//        	Util.print("byte:%x int:%d", m, (int)(m & 0xFF));
        	eppArray = Util.extractErrorPutternPerPix(table[(int)(m & 0xFF)], n);
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
	private static byte[] extracting(byte[] stego, byte[] cover, int offset, int[] table, HashMap<Integer, Integer> anti, int n, int length) {
		byte[] msg = new byte[length];
		byte eBit, ep = 0;
		int index = 0;
		
		for(int i=offset; i<stego.length; i+=n) {
			for(int j=i; j<i+n; j++) {
				eBit = (byte) ((stego[j] ^ cover[j]) & 0x01);
				// とりだしたビットのXORをepのLSBに格納し1ビット左シフトすることで誤りパターンを取り出す
				ep = (byte) ((ep << 1) | eBit) ;
			}
			
			// 誤りパターンから埋め込みデータを復元する
			int key = ep & 0xff;
			Integer value = anti.get(key);
			msg[index] = value.byteValue();
			index++;
			ep = 0;
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
				Util.println(String.format("i: %d, msg1:%d, msg2:%d",i, msg1[i], msg2[i]));
				flag = false;
//				break;
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
