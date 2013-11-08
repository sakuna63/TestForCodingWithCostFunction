import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
	private static final String BURIED_IMAGE_PATH = "./embeded_img/";
	private static final String CSV_FILE_PATH = "./csv/";
	private static final String CHARACTER_CODE = "US-ASCII";
	
	// CHARACTER_CODEのサイズ
	private static final int CHARACTER_SIZE = 8;
	// 画像の一辺のサイズ
	private static final int IMAGE_SIZE = 256;
	
	private static final int[] ERROR_CODE_LENGTHS = new int[]{
		8, 16, 24, 32, 40, 48, 56, 64, 72, 80, 88, 96, 
		104, 112, 120, 128, 136, 144, 152, 160, 168, 176,
		184, 192, 200, 208, 216, 224, 232, 240, 248, 256
	};
	
	public static void main(String[] args) {
		int[] msg = getRandomTextByte(0, IMAGE_SIZE * IMAGE_SIZE);
		File imgDir = new File(IMAGE_PATH);
		File f = imgDir.listFiles()[0];
		CoverData cover;
		PrintWriter pw;
		
//		for(File f : imgDir.listFiles()) {
//			for(int seed=0; seed<=32; seed++) {
				cover = new CoverData(f);
				pw = getPrintWriter(cover.name);
				pw.println("誤りパターン長, 埋め込み範囲, 埋め込み率,PSNR,誤り率");
				
				
				for(int i : ERROR_CODE_LENGTHS) {
					for(int j=1; j<=8; j++) {
						execEmbedingProcess(cover, pw, msg, i, j);
					}
				}
				
				pw.close();
//			}
//		}
		
		Util.print("埋め込み終了");
    }

	/**
	 * ステゴデータの生成プロセスを実行する
	 * @param file
	 * @param pw
	 * @param msg
	 * @param codeLength
	 * @param range
	 */
	private static void execEmbedingProcess(CoverData cover, PrintWriter pw, int[] msg, int codeLength, int range) {
		
		Util.println("run codeLength:%d range:%d", codeLength, range);
		
		StegoData stego = cover.embeding(msg, codeLength, range);
		stego.output(BURIED_IMAGE_PATH + codeLength + "_" + range + "_" + cover.name + ".bmp");
		
		double psnr = stego.calcPSNR(cover);
		pw.println(codeLength + "," + range + "," + ((double)8/codeLength) * 100 + "," + psnr + "," + stego.getErrorRate());
		
		int[] eMsg = stego.extracting(cover);
		
		if( !compMsg(msg, eMsg, codeLength, range) )
			Util.println("メッセージの取り出しに失敗しました");

	}

	/**
	 * ２つのメッセージを比較する
	 * @param msg1
	 * @param msg2
	 * @return
	 */
	private static boolean compMsg(int[] origin, int[] embuded, int codeLength, int range) {
		boolean flag = true;
		
		for(int i=0; i<embuded.length; i++) {
			if( origin[i] != embuded[i]) {
				Util.println(String.format("length:%d, range:%d, i: %d, origin:%d, embuded:%d",codeLength, range, i, origin[i], embuded[i]));
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
	 * PrinterWriterのインスタンスを取得する
	 * @param fileName
	 * @return
	 */
	
	private static PrintWriter getPrintWriter(String fileName) {
		String name = CSV_FILE_PATH + fileName + ".csv";
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new OutputStreamWriter
					(new FileOutputStream(new File(name)),
							"Shift_JIS"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pw;
	}

}
