import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import my.util.Calc;
import my.util.Util;


class StegoData {

	def imgBuff, errorRate = 0, offset, codeLength
	
	public StegoData(CoverData cover, codeLength) {
		this.imgBuff = cover.imgBuff.clone()
		this.offset = cover.offset
		this.codeLength = codeLength;
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
		def msg = [], ep, max = (imgBuff.size() - offset)/codeLength -1
		
		for(i in 0..max) {
			ep = Util.extractErrorPattern(imgBuff, cover.imgBuff, i * codeLength + offset, codeLength);
			// 誤りパターンから埋め込みデータを復元する
			msg.add(Util.error2Message(codeLength, ep));
		}
		
		return msg;
	}
}
