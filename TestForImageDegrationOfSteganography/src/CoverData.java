import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import my.util.Util;


public class CoverData {
	
	public byte[] buff;
	public int buff_offset;
	public String file_name;

	public CoverData(File file) {
		File2Buff(file);
		this.file_name = file.getName();
	}

	public StegoData embeding(int[] msg, int code_length, int range) {
		StegoData stego = new StegoData(this, code_length, range);
		int enable_length = (buff.length - buff_offset) * range / code_length;
		
		for (int i=0; i<enable_length; i++) {
			int[] error = Util.message2Error(msg[i], code_length);
			byte[] error_arr = Util.splitError(error, code_length);
			
			embedingError(stego, error_arr, i, code_length);
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
			buff_offset = offsetBuff[3] << 24 | offsetBuff[2] << 16 | offsetBuff[1] << 8 | offsetBuff[0];
			
			// バッファにビットマップを読み込む
			buff = new byte[size];
			
			fis.close();
			fis = new FileInputStream(file);
			
			fis.read(buff);
			fis.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void embedingError(StegoData stego, byte[] error_arr, int msg_index, int code_length) {
		int start = msg_index * code_length;
		int enable_length_per_bit_space = buff.length - buff_offset;
		int i = 0;
		for(byte e: error_arr) {
			int pos = (start + i) % enable_length_per_bit_space + buff_offset;
			int shift = (start + i) / enable_length_per_bit_space;
			stego.buff[pos] = (byte) (stego.buff[pos] ^ ( e << shift));
			stego.error_rate += e;
			i++;
		}
	}
}
