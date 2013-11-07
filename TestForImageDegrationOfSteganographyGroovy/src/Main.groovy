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

ERROR_CODE_LENGTHS = [
	8, 16, 24, 32, 40, 48, 56, 64, 72, 80, 88, 96, 
	104, 112, 120, 128, 136, 144, 152, 160, 168, 176,
	184, 192, 200, 208, 216, 224, 232, 240, 248, 256
]

def imgDir = new File(IMAGE_PATH)
def pw, cover, msg, msgLength
def f = imgDir.listFiles()[0];
//imgDir.listFiles().each {f->

	cover = new CoverData(f)
	pw = getPrintWriter(cover.name)
	pw.println("誤りパターン長,埋め込み率,PSNR,誤り率");
	
	msg = getRandomTextByte(0, IMAGE_SIZE * IMAGE_SIZE / 8)
	
	ERROR_CODE_LENGTHS.each {length ->
		execEmbedingProcess(cover, pw, msg, length)
	}

	pw.close()
//	throw new Exception();
//}

println "success"
/**
 * ステゴデータの生成プロセスを実行する
 * @param file
 * @param pw
 * @param msg
 * @param table
 * @param msgLength
 * @param codeLength
 */
def execEmbedingProcess(CoverData cover, pw, msg, codeLength) {
	StegoData stego = cover.embeding(msg, codeLength)
	stego.output("$BURIED_IMAGE_PATH$codeLength$cover.name" + ".bmp")

	def psnr = stego.calcPSNR(cover)
	pw.println(codeLength + "," + ((double)8/codeLength) * 100 + "," + psnr + "," + stego.errorRate)
	
	def eMsg = stego.extracting(cover)
	
	if( !compMsg(msg, eMsg, codeLength) )
		println "メッセージの取り出しに失敗しました"
}

/**
 * ２つのメッセージを比較する
 * @param msg1
 * @param msg2
 * @return
 */
def compMsg(origin, embuded, codeLength) {
	def flag = true
	
	for(i in 0..embuded.size()-1)
		if( origin[i] != embuded[i]) {
			println String.format("length:%d, i: %d, origin:%d, embuded:%d",codeLength, i, origin[i], embuded[i])
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