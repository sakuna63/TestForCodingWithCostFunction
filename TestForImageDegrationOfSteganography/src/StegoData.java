import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import my.util.Calc;
import my.util.Util;


class StegoData {

	byte[] buff;
	int error_rate = 0, buff_offset, code_length, range;
	
	public StegoData(CoverData cover, int codeLength, int range) {
		this.buff = cover.buff.clone();
		this.buff_offset = cover.buff_offset;
		this.code_length = codeLength;
		this.range = range;
	}
	
	public void output(String name) {
		File stegoFile = new File(name);
		
		if( stegoFile.exists() ) ;
			stegoFile.delete();
		
		try {
			stegoFile.createNewFile();
			
			FileOutputStream output = new FileOutputStream(stegoFile);
			output.write(buff);
			output.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public double psnr(CoverData cover) {
		return Calc.PSNR(buff, cover.buff, buff_offset);
	}
	
	public double ssim(CoverData cover) {
		return Calc.SSIM(new Calc.SOption(), buff, cover.buff, buff_offset);
	}
	
	public int[] extracting(CoverData cover) {
		// LSB~rangeビット列空間に埋め込み可能な文字数-1
		int enable_length = (buff.length - buff_offset) * range / code_length;
		int[] msg = new int[enable_length];
		
		for(int i=0; i<enable_length; i++) {
			int[] error = extractError(cover.buff, i);
			// 誤りパターンから埋め込みデータを復元する
			msg[i] = (Util.error2Message(error, code_length));
		}
		
		return msg;
	}

	public double getErrorRate() {
		return (double) error_rate * 100 / (buff.length - buff_offset) / range;
	}
	
	/**
     * ステゴデータから埋め込み誤りパターンを抽出する
     * @param cover_buff
     * @param msg_index
     * @return
     */
    private int[] extractError(byte[] cover_buff, int msg_index) { 
        int[] code = new int[8];
        int start = msg_index * code_length;
        int enable_length_per_bit_space = buff.length - buff_offset;
        for(int i=0; i < code_length; i++) {
            int pos = (start + i) % enable_length_per_bit_space + buff_offset;
            int bit = (start + i) / enable_length_per_bit_space;
            byte error_bit = Util.extractErrorBit(buff[pos], cover_buff[pos], bit);
            // 誤りビットが1だったとき、対応するビットを1にする
            if (error_bit == 0x01) Util.raiseBit(code, code_length - i - 1);
        }
        return code;
    }
}
