import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import my.util.Calc;
import my.util.Util;

IMAGE_PATH = "./img/"
BURIED_IMAGE_PATH = "./embeded_img/"
CSV_FILE_PATH = "./csv/"
CHARACTER_CODE = "US-ASCII"

// CHARACTER_CODEのサイズ
CHARACTER_SIZE = 8
// 画像の一辺のサイズ
IMAGE_SIZE = 256

def imgDir = new File(IMAGE_PATH)
def pw, cover, msg, msgLength
//def f = imgDir.listFiles()[0];
imgDir.listFiles().each {f->

	cover = new CoverData(f)
	pw = getPrintWriter(cover.name)
	pw.println("誤りパターン長, 埋め込み範囲, 埋め込み率,PSNR,誤り率");
	
	msg = getRandomTextByte(0, IMAGE_SIZE * IMAGE_SIZE)
	
	for(i in 8..256)
		for(j in 1..8)
			execEmbedingProcess(cover, pw, msg, i, j)

	pw.close()
}

println "success"
/**
 * ステゴデータの生成プロセスを実行する
 * @param file
 * @param pw
 * @param msg
 * @param codeLength
 * @param range
 */
def execEmbedingProcess(CoverData cover, pw, msg, codeLength, range) {
	StegoData stego = cover.embeding(msg, codeLength, range)
	stego.output("$BURIED_IMAGE_PATH$codeLength" + "_" + range + "_" + cover.name + ".bmp")

	def psnr = stego.calcPSNR(cover)
	pw.println("$codeLength,$range," + ((double)8/codeLength) * 100 + ",$psnr,$stego.errorRate")
	
	def eMsg = stego.extracting(cover)
	
	if( !compMsg(msg, eMsg, codeLength, range) )
		println "メッセージの取り出しに失敗しました"
}

/**
 * ２つのメッセージを比較する
 * @param msg1
 * @param msg2
 * @return
 */
def compMsg(origin, embuded, codeLength, range) {
	def flag = true
	
	for(i in 0..embuded.size()-1)
		if( origin[i] != embuded[i]) {
			println String.format("length:%d, range:%d i: %d, origin:%d, embuded:%d",codeLength, range, i, origin[i], embuded[i])
			flag = false
			break;
		}
	
	return flag
}

/**
 * 乱数列を生成する
 * @param textNum
 * @return
 */
def getRandomTextByte(seed, textNum) {
	Sfmt s = new Sfmt(seed)
	def randByteArray = []

	for( i in 0..textNum-1 )
		randByteArray.add(s.NextInt((int) Math.pow(2, CHARACTER_SIZE)))
	
	return randByteArray
}


/**
 * PrinterWriterのインスタンスを取得する
 * @param fileName
 * @return
 */
def getPrintWriter(fileName) {
	def name = "$CSV_FILE_PATH$fileName" +".csv"
	return new PrintWriter(new OutputStreamWriter(
			new FileOutputStream(new File(name)),"Shift_JIS"))
}