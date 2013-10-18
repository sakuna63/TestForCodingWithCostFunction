package my.util;

public class Calc {

	/**
	 * 0~maxまでの数値に対応したnビット誤りパターンテーブルを返す
	 * @param n
	 * @param max
	 * @return
	 */
	public static int[] errorPatternTable(int n, int max) {
		int[] table = new int[max];
		int index = 0, combNum, pastCombNum = 0;
		
		// ハミングウェイトを変化させる
		for(int i=0; i<n; i++) {
			// nCiを計算
			combNum = combination(n, i);
			// 0~nCiまでに対応するnビットでハミングウェイとiのパターンを決定する
			for(int j=0; j<combNum; j++) {
				table[index] = countableCode(n, i, j);// + pastCombNum;
				index++;
				if(index >= max) break;
			}
			if(index >= max) break;
			pastCombNum += combNum;
		}
		return table;
	}
	
	/**
	 * パスカルの三角形を逆算して、番号numに対応する長さn, ハミングウェイトkの２進数を返す(byte形式)
	 * @param n
	 * @param k
	 * @param num
	 * @return
	 */
	public static int countableCode(int n, int k, int num) {
		int code = 0;
		int pascalNum;
		// パスカルの三角形上の座標
		while( !(n<0) ) {
			// 右斜め上に移動
			pascalNum = combination(n-1, k);
//			System.out.println(pascalNum);
			if(pascalNum > num) {
				n--;
			}
			// 左斜め上に移動
			else {
				n--;
				k--;
				num -= pascalNum;
				code = (int) (code + Math.pow(2, n));
			}
		}
		return code;
	}
	
	/**
	 * 底が２のlogの値を返す
	 * @param num
	 * @return
	 */
	public static double log2(double num) {
		return Math.log(num) / Math.log(2);
	}
	
	public static int factorial(int n) {
		return n <= 1 ? 1 : n * factorial(n-1);
	}
	
	public static int combination(int n, int k) {
		if( n < k ) return 0;
		return factorial(n) / (factorial(k) * factorial(n-k));
	}
	
	public static int hamingWeight(int num) {
		int weight = 0;
		while(num != 0) {
			if(num%2 == 1) {
				weight++;
			}
			num /=2;
		}
		return weight;
	}
}
