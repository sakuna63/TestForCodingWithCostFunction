package my.util;

public class Util {

	private static final int MASK = 0x0000000000000000000000000000000000000000000000000000000000000001;
	
	public static int error2Message(int[] error, int error_length) {
		int haming_weight = hamingWeight(error);
		int	offset = calcOffset(error_length, haming_weight);
		
		return antiCountableCode(error_length, haming_weight, error) + offset;
	}
	
	public static int[] message2Error(int msg, int error_length) {
		int[] info = message2WeightAndNum(msg, error_length);
		return countableCode(error_length, info[0], info[1]);
	}

	public static int[] message2WeightAndNum(int msg, int error_length) {
		int i, combNum;
		for(i=0; i<=error_length; i++) {
			combNum = (int) Calc.combination(error_length, i);
			if( msg < combNum ) break;
			msg -= combNum;
		}
		return new int[]{i, msg};
	}
	
	/**
	 * nC0 〜 nCk-1までの合計値（数え上げ符号のoffset）を計算する
	 * @param n
	 * @param k
	 * @return
	 */
	public static int calcOffset(int n, int k) {
		int offset = 0;
		for(int i=0; i<k; i++) {
			offset += Calc.combination(n, i);
		}
		return offset;
	}
	
	/**
	 * 10101010 → [1,0,1,0,1,0,1,0]
	 * @param error
	 * @param length
	 * @return
	 */
	public static byte[] splitError(int[] error, int length) {
		byte[] error_arr = new byte[length];
		for(int i=0; i<length; i++) {
			// MSBが配列の0番目に来るようにする
			error_arr[i] = extractBit(error, length - i - 1);
		}
		return error_arr;
	}
	
	/**
	 * errorのnビット目をLSBにして取り出す
	 * @param error
	 * @param n
	 * @return
	 */
	public static byte extractBit(int error, int n) {
		return (byte) ((error >>> n) & MASK);
	}
	
	/**
	 * errorのnビット目をLSBにして取り出す
	 * @param error
	 * @param n
	 * @return
	 */
	public static byte extractBit(int[] error, int n) {
		int index = n/32;
		int shift_num = n%32;
		return (byte) ((error[index] >>> shift_num) & MASK);
	}
	
	/**
	 * ピクセルからnビット目の誤りビットを取り出す
	 * @param stego_px
	 * @param cover_px
	 * @param n
	 * @return
	 */
	public static byte extractErrorBit(byte stego_px, byte cover_px, int n) {
		// 誤りビットを抽出する
		byte stego_bit = (byte) ((stego_px & (0x01 << n)) >>> n);
		byte cover_bit = (byte) ((cover_px & (0x01 << n)) >>> n);
		return (byte) ((stego_bit ^ cover_bit) & 0x01);
	}
	
	
	/**
	 * パスカルの三角形を逆算して、番号numに対応する長さlength, ハミングウェイトweightの２進数を返す(byte形式)
	 * @param length
	 * @param weight
	 * @param num
	 * @return
	 */
	public static int[] countableCode(int length, int weight, long num) {
		int[] code = new int[8];
		long pascal_num;
		
		while( !(length < 0) ) {
			// 右斜め上の値を計算
			pascal_num = Calc.combination(length, weight);
			
			if(pascal_num <= num) {
				weight--;
				num -= pascal_num;
				raiseBit(code, length);
			}
			
			length--;
		}
		return code;
	}

	/**
	 * パスカルの三角形を利用して数え上げ符号を対応する値に変換する
	 * @param length
	 * @param weight
	 * @param code
	 * @return
	 */
	public static int antiCountableCode(int length, int weight, int[] code) {
		int num = 0;
		
		byte bit;
		for(int j=length-1; j>=0; j--) {
			// i番目のビットを取り出す
			bit = Util.extractBit(code, j);
			// もしLSBが１なら左斜め上に移動
			if( bit == 0x01 ) {
				// 右斜め上の数値を記録する
				num += Calc.combination(j, weight);
				weight--;
			} 
		}
		return num;
	}
	
	/**
	 * codeのnビット目を1にする
	 * @param code
	 * @param n
	 */
	public static void raiseBit(int[] code, int n) {
		int index = n/32;
		int shiftNum = n%32;
		int mask = (MASK << shiftNum);
		code[index] = code[index] | mask;
	}
	
	/**
	 * arrayのハミングウェイとを計算する
	 * @param array
	 * @return
	 */
	public static int hamingWeight(int[] array) {
		int weight = 0;
		for(int num : array) {
			weight += hamingWeight(num);
		}
		return weight;
	}
	
	/**
	 * numのハミングウェイトを計算する
	 * @param num
	 * @return
	 */
	public static int hamingWeight(int num) {
		int weight = 0;
		for(int i=0; i<32; i++) {
			if( Util.extractBit(num, i) == 0x01 ) weight++;
		}
		return weight;
	}

    public static int[] calcTargetBits(int num) {
        char[] binary = Integer.toBinaryString(num).toCharArray();
        int count_target = countItemNumInTraget(binary, '1');
        int[] targets = new int[count_target];
        int index_t = 0;
        for (int i=binary.length-1; i >=0; i--) {
            if(binary[i] == '1'){
                targets[index_t] = binary.length - i - 1;
                index_t++;
            }
        }
        return targets;
    }

    private static int countItemNumInTraget(char[] target, char item) {
        int count = 0;
        for(char c : target) {
            count += c == item ? 1 : 0;
        }
        return count;
    }
}
