import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import my.util.Util;

public class Main {
	private static final String IMAGE_PATH = "./img/";
	private static final String BURIED_IMAGE_PATH = "./embeded_img/";
	private static final String CSV_FILE_PATH = "./csv/";
	
	// CHARACTER_CODEのサイズ
	private static final int CHARACTER_SIZE = 8;
	// 画像の一辺のサイズ
	private static final int IMAGE_SIZE = 256;
	
	public static void main(String[] args) {
		int[] msg = getRandomTextByte(0, IMAGE_SIZE * IMAGE_SIZE);
		File imgDir = new File(IMAGE_PATH);
		CoverData cover;
		PrintWriter pw;
		
		for(File f : imgDir.listFiles()) {
//			for(int seed=0; seed<=32; seed++) {
				cover = new CoverData(f);
				pw = getPrintWriter(cover.name);
				pw.println("誤りパターン長, 埋め込み範囲, 埋め込み率,PSNR,SSIM,誤り率");
				
				
//				for(int i=8; i<=256; i++) {
//					for(int j=1; j<=8; j++) {
				int i=8, j=1;
					execEmbedingProcess(cover, pw, msg, i, j);
//					}
//				}
				
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
	 * @param codeLength
	 * @param range
	 */
	private static void execEmbedingProcess(CoverData cover, PrintWriter pw, int[] msg, int codeLength, int range) {
		
		Util.println("run codeLength:%d range:%d", codeLength, range);
		
		StegoData stego = cover.embeding(msg, codeLength, range);
		stego.output(BURIED_IMAGE_PATH + codeLength + "_" + range + "_" + cover.name + ".bmp");
		
		pw.println(codeLength + "," + range + "," + ((double)8/codeLength) * 100 + ","
						+ stego.psnr(cover) + "," + stego.ssim(cover) + "," + stego.getErrorRate());
		
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
