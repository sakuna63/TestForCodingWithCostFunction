import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedList;

import my.util.IO;

public class Main {
	private static final String IMAGE_PATH = "./img/";
	private static final String EMBEDED_IMAGE_PATH = "./embeded_img/";
	private static final String CSV_FILE_PATH1 = "./csv_base_emebding_area/";
	private static final String CSV_FILE_PATH2 = "./csv_base_errorlength/";
	private static final String CSV_FILE_PATH3 = "./csv_base_image/";
	
	// CHARACTER_CODEのサイズ
	private static final int CHARACTER_SIZE = 8;
	// 画像の一辺のサイズ
	private static final int IMAGE_SIZE = 256;
	
	public static void main(String[] args) {
		int[] msg = createMsg(0, IMAGE_SIZE * IMAGE_SIZE);
		File img_dir = new File(IMAGE_PATH);
		
//		exec1(msg, img_dir);
//		exec2(msg, img_dir);
//		exec3(msg, img_dir);
		Main2.run();
		
		IO.print("埋め込み終了");
    }
	
	private static void exec1(int[] msg, File img_dir) {
		PrintWriter pw;
		CoverData cover;
		File f = img_dir.listFiles()[0];
//		for(File f : imgDir.listFiles()) {
			cover = new CoverData(f);
			pw = getPrintWriter(CSV_FILE_PATH1, cover.file_name.replace(".bmp", ""));
			pw.println("埋め込み範囲, 埋め込み率,PSNR,SSIM,誤り率");
			

			for(int range=1; range<=8; range++) {
				for(int length=8; length<=256; length++) {
					pw.print(range + ",");
					execEmbedingProcess(cover, pw, msg, length, range);
				}
			}
			
			pw.close();
//		}
	}

	private static void exec2(int[] msg, File img_dir) {
		PrintWriter pw;
		CoverData cover;
		File f = img_dir.listFiles()[0];
//		for(File f : imgDir.listFiles()) {
			cover = new CoverData(f);
			pw = getPrintWriter(CSV_FILE_PATH2, cover.file_name.replace(".bmp", ""));
			pw.println("埋め込み範囲, 埋め込み率,PSNR,SSIM,誤り率");

			for(int length=8; length<=256; length++) {
				for(int range=1; range<=8; range++) {
					pw.print(range + ",");
					execEmbedingProcess(cover, pw, msg, length, range);
				}
			}
			
			pw.close();
//		}
	}
	
	private static void exec3(int[] msg, File img_dir) {
		PrintWriter pw;
		LinkedList<CoverData> covers = new LinkedList<>();
		
		for(File f : img_dir.listFiles()) {
			covers.add(new CoverData(f));
		}

		for(int range=2; range<=8; range++) {
			pw = getPrintWriter(CSV_FILE_PATH3, "" + range);
			pw.print(",");
			for(CoverData cover : covers) {
				pw.print(cover.file_name + ",");
			}
			pw.println();
			for(int length=8; length<=256; length++) {
				pw.print(((double)8/length) * 100 + ",");
				for(CoverData cover : covers) {
					execEmbedingProcess3(cover, pw, msg, length, range);
				}
				pw.println();
			}
			pw.close();
		}
	}
	
	/**
	 * ステゴデータの生成プロセスを実行する
	 * @param file
	 * @param pw
	 * @param msg
	 * @param codeLength
	 * @param range
	 */
	private static void execEmbedingProcess(CoverData cover, PrintWriter pw, int[] msg, int code_length, int range) {
		
		IO.println("run codeLength:%d range:%d", code_length, range);
		
		StegoData stego = cover.embeding(msg, code_length, range);
		stego.output(EMBEDED_IMAGE_PATH + code_length + "_" + range + "_" + cover.file_name);
		
//		pw.println(codeLength + "," + range + "," + ((double)8/codeLength) * 100 + ","
//						+ stego.psnr(cover) + "," + stego.ssim(cover) + "," + stego.getErrorRate());
		pw.println(((double)8/code_length) * 100 + ","
						+ stego.psnr(cover) + "," + stego.ssim(cover) + "," + stego.getErrorRate());
		
		int[] eMsg = stego.extracting(cover);
		
		if( !compMsg(msg, eMsg, code_length, range) )
			IO.println("メッセージの取り出しに失敗しました");
	}
	
	/**
	 * ステゴデータの生成プロセスを実行する
	 * @param file
	 * @param pw
	 * @param msg
	 * @param codeLength
	 * @param range
	 */
	private static void execEmbedingProcess3(CoverData cover, PrintWriter pw, int[] msg, int code_length, int range) {
		
		IO.println("run codeLength:%d range:%d", code_length, range);
		
		StegoData stego = cover.embeding(msg, code_length, range);
		stego.output(EMBEDED_IMAGE_PATH + code_length + "_" + range + "_" + cover.file_name);
		
		pw.print(stego.ssim(cover) + ",");
		
		int[] eMsg = stego.extracting(cover);
		
		if( !compMsg(msg, eMsg, code_length, range) )
			IO.println("メッセージの取り出しに失敗しました");
	}
	
	/**
	 * ２つのメッセージを比較する
	 * @param msg1
	 * @param msg2
	 * @return
	 */
	private static boolean compMsg(int[] origin, int[] embuded, int code_length, int range) {
		for (int i=0; i<embuded.length; i++) {
			if( origin[i] != embuded[i]) {
				IO.println(String.format("length:%d, range:%d, i: %d, origin:%d, embuded:%d",code_length, range, i, origin[i], embuded[i]));
				return false;
			}
		}
		return true;
	}

	/**
	 * 乱数列を生成する
	 * @param num
	 * @return
	 */
	private static int[] createMsg(int seed, int num) {
		Sfmt s = new Sfmt(seed);
		int[] randByteArray = new int[num];
	
		for( int i=0; i<num; i++)
		    randByteArray[i] = s.NextInt((int) Math.pow(2, CHARACTER_SIZE));
		
		return randByteArray;
    }

	/**
	 * PrinterWriterのインスタンスを取得する
	 * @param fileName
	 * @return
	 */
	
	private static PrintWriter getPrintWriter(String file_path, String file_name) {
		String pw_name = file_path + file_name + ".csv";
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new OutputStreamWriter
					(new FileOutputStream(new File(pw_name)),
							"Shift_JIS"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pw;
	}

}
