import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import my.util.Util;


public class CoverData {
	
	public byte[] imgBuff;
	public int offset;
	public String name;

	public CoverData(File file) {
		File2Buff(file);
		this.name = file.getName().replace(".bmp", "");
	}

	public StegoData embeding(int[] msg, int codeLength, int range) {
		StegoData stego = new StegoData(this, codeLength, range);
		int[] ep = null;
		byte[] eppArray;
		int pos, msgPos, bit;
		// bit空間ごとの埋め込み可能な文字数
		int msgLengthPerBit = (imgBuff.length - offset) / codeLength;
		// LSB~rangeビット列空間全体で見たときに埋め込み可能な文字数-1
		int msgLength = msgLengthPerBit * range;
		
		for(int i=0; i<msgLength; i++) {
			// 誤りパターンを生成
			ep = Util.message2Error(msg[i], codeLength);
			// 誤りパターンを1ビットごとに8ビット列に分解する、eppArray[0]が誤りパターンのLSB
			eppArray = Util.extractErrorPatternPerPix(ep, codeLength);
			int j = 0;
			for(int e: eppArray) {
				stego.errorRate += e;
				// メッセージバイナリを1次元的に見た時の座標
				msgPos = (i+1) * codeLength - j - 1;
				// 埋め込み対象となる画像ビット列空間の座標
				pos = msgPos % (msgLengthPerBit * codeLength) + offset;
				bit = msgPos / (msgLengthPerBit * codeLength);
				stego.imgBuff[pos] = (byte) (stego.imgBuff[pos] ^ ( e << bit));
				j++;
			}
		}
		return stego;
	}
	
	private void File2Buff(File file) {
		byte[] sizeBuff = new byte[4], offsetBuff = new byte[4];

		try {
			FileInputStream fis = new FileInputStream(file);
			
			// 画像のサイズを読み込む
			fis.skip(2);
			fis.read(sizeBuff);
			int size = sizeBuff[3] << 24 | sizeBuff[2] << 16 | sizeBuff[1] << 8 | sizeBuff[0];
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
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
