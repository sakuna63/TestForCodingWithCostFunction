import java.io.FileInputStream;

import my.util.Util;

class CoverData {
	def imgBuff, offset, name
	
	CoverData(File file) {
		File2Buff(file)
		this.name = file.name.replace(".bmp", "")
	}
	
	def embeding(msg, codeLength) {
		def ep, eppArray, msgLength = (imgBuff.size() - offset)/codeLength -1
		def stego = new StegoData(this, codeLength)
		
		for(i in 0..msgLength) {
			ep = Util.message2Error(msg[i], codeLength)
			
			eppArray = Util.extractErrorPutternPerPix(ep, codeLength)
			eppArray.eachWithIndex {e, j->
				stego.errorRate += e
				stego.imgBuff[(i+1) * codeLength + offset - j - 1] = stego.imgBuff[(i+1) * codeLength + offset - j - 1] ^ e
				if(!imgBuff[i * codeLength + j + offset].is(stego.imgBuff[i * codeLength + j + offset])) println "before:" + imgBuff[i + j + offset] + " after:" + stego.imgBuff[i + j + offset]
			}
		}
		
		return stego;
	}
	
	def File2Buff(file) {
		def sizeBuff = new byte[4], offsetBuff = new byte[4];

		def fis = new FileInputStream(file)
		
		// 画像のサイズを読み込む
		fis.skip(2);
		fis.read(sizeBuff);
		def size = sizeBuff[3] << 24 | sizeBuff[2] << 16 | sizeBuff[1] << 8 | sizeBuff[0];
		// 画像のオフセットを読み込む
		fis.skip(4);
		fis.read(offsetBuff);
		offset = offsetBuff[3] << 24 | offsetBuff[2] << 16 | offsetBuff[1] << 8 | offsetBuff[0];
		
		// バッファにビットマップを読み込む
		imgBuff = new byte[size];
		
		fis.close();
		fis = new FileInputStream(file);
		
		fis.read(imgBuff);
		fis.close();
	}
}