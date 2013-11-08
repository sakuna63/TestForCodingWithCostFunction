import java.io.FileInputStream;

import my.util.Util;

class CoverData {
	def imgBuff, offset, name
	
	CoverData(File file) {
		File2Buff(file)
		this.name = file.name.replace(".bmp", "")
	}
	
	def embeding(msg, codeLength, range) {
		def stego = new StegoData(this, codeLength, range)
		def ep, eppArray, pos, msgPos, bit
		// bit空間ごとの埋め込み可能な文字数
		def msgLengthPerBit = (imgBuff.size() - offset) / codeLength as Integer
		// LSB~rangeビット列空間全体で見たときに埋め込み可能な文字数-1
		def msgLength = msgLengthPerBit * range -1
		
		for(i in 0..msgLength) {
			// 誤りパターンを生成
			ep = Util.message2Error(msg[i], codeLength)
			// 誤りパターンを1ビットごとに8ビット列に分解する、eppArray[0]が誤りパターンのLSB
			eppArray = Util.extractErrorPatternPerPix(ep, codeLength)
			eppArray.eachWithIndex {e, j->
				stego.errorRate += e
				// メッセージバイナリを1次元的に見た時の座標
				msgPos = (i+1) * codeLength - j - 1 as Integer
				// 埋め込み対象となる画像ビット列空間の座標
				pos = msgPos % (msgLengthPerBit * codeLength) + offset as Integer
				bit = msgPos / (msgLengthPerBit * codeLength) as Integer
				stego.imgBuff[pos] = stego.imgBuff[pos] ^ ( e << bit)
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