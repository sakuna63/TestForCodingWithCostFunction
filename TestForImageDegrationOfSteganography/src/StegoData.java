import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import my.util.Calc;
import my.util.Util;


class StegoData {

	byte[] imgBuff;
	int errorRate = 0, offset, codeLength, range;
	
	public StegoData(CoverData cover, int codeLength, int range) {
		this.imgBuff = cover.imgBuff.clone();
		this.offset = cover.offset;
		this.codeLength = codeLength;
		this.range = range;
	}
	
	public void output(String name) {
		// 画像を出力する
		File stegoFile = new File(name);
		
		if( stegoFile.exists() ) ;
			stegoFile.delete();
		
		try {
			stegoFile.createNewFile();
			
			FileOutputStream output = new FileOutputStream(stegoFile);
			output.write(imgBuff);
			output.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public double calcPSNR(CoverData cover) {
		return Calc.PSNR(imgBuff, cover.imgBuff, offset);
	}
	
	public int[] extracting(CoverData cover) {
		// bit空間ごとの埋め込み可能な文字数
		int msgLengthPerBit = (imgBuff.length - offset) / codeLength;
		// LSB~rangeビット列空間に埋め込み可能な文字数-1
		int msgLength = msgLengthPerBit * range;
		int[] ep, msg = new int[msgLength];
		
		for(int i=0; i<msgLength; i++) {
			ep = Util.extractErrorPattern(imgBuff, cover.imgBuff, i * codeLength, msgLengthPerBit, offset, codeLength);
			// 誤りパターンから埋め込みデータを復元する
			msg[i] = (Util.error2Message(codeLength, ep));
		}
		
		return msg;
	}

	public double getErrorRate() {
		return (double) errorRate * 100 / (imgBuff.length - offset) / range;
	}
}
