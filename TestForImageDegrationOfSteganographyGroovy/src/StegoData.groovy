import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import my.util.Calc;
import my.util.Util;


class StegoData {

	def imgBuff, errorRate = 0, offset, codeLength, range
	
	public StegoData(CoverData cover, codeLength, range) {
		this.imgBuff = cover.imgBuff.clone()
		this.offset = cover.offset
		this.codeLength = codeLength
		this.range = range
	}
	
	def output(name) {
		// 画像を出力する
		def stegoFile = new File(name)
		
		if( stegoFile.exists() ) 
			stegoFile.delete()
		
		stegoFile.createNewFile()
		
		def output = new FileOutputStream(stegoFile)
		output.write(imgBuff)
		output.close()
	}
	
	def calcPSNR(CoverData cover) {
		return Calc.PSNR(imgBuff, cover.imgBuff, offset)
	}
	
	def extracting(CoverData cover) {
		def msg = [], ep
		// bit空間ごとの埋め込み可能な文字数
		def msgLengthPerBit = (imgBuff.size() - offset) / codeLength as Integer
		// LSB~rangeビット列空間に埋め込み可能な文字数-1
		def msgLength = msgLengthPerBit * range - 1
		
		for(i in 0..msgLength) {
			ep = Util.extractErrorPattern(imgBuff, cover.imgBuff, i * codeLength, msgLengthPerBit, offset, codeLength);
			// 誤りパターンから埋め込みデータを復元する
//			def m = Util.error2Message(codeLength, ep);
//			println "ep:$ep[0] msg:$m"
			msg.add(Util.error2Message(codeLength, ep));
		}
		
		return msg;
	}

	def getErrorRate() {
		return errorRate / ((imgBuff.size() - offset) * range) * 100
	}
}
