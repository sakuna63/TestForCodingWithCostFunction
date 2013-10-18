import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * US-ASCIIを用いることを前提にコードを書く
 * @author sakuna63
 *
 */
import javax.imageio.ImageIO;

public class Main {
	private static final String IMAGE_PATH = "./img/";
	private static final String CHARACTER_CODE = "US-ASCII";
	private static final int TEXT_LENGTH = 256;
	
	public static void main(String[] args) {
		byte[] textBuff = getRandomTextByte(256);
		byte[] table = errorPatternTable(8, 95);
//		String text = getRandomText(TEXT_LENGTH);
//		System.out.println( text );
		
		File imgDir = new File(IMAGE_PATH);
//		for(String name : imgDir.list()) {
//			System.out.println(name);
//		}
		
		FileInputStream in = null;
		byte[] buff = new byte[30];
		try {
			in = new FileInputStream(imgDir.listFiles()[0]);
			if( in != null ) {	
				in.read(buff);
				in.close();
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		print(new String(buff));
		for(int k=0; k<buff.length; k++)
			print(buff[k]);
		
		
		BufferedImage img = null;
		try {
			img = ImageIO.read(imgDir.listFiles()[0]);
//			img = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		} catch (IOException e) {
			e.printStackTrace();
		}

//		print("pixel:" + img.getData().getNumDataElements());
		
		int argb, a, r, g, b;
		for(int i=0; i<img.getHeight(); i++) {
			for(int j=0; j<img.getWidth(); j++) {
				argb = img.getRGB(i, j);
				a = (argb>>>24) & 0xff;
				r = (argb>>>16) & 0xff;
				g = (argb>>> 8) & 0xff;
				b = argb        & 0xff;
//				System.out.println("argb:"+argb+"a:"+a+" r:"+r+" g:"+g+" b:"+b);
//				print("pixel("+i+","+j+"):" + img.getRGB(i, j));
			}
		}
    }
	
	private static <T> void print(T text) {
		System.out.println(text);
	}

	
	/**
	 * 0~maxまでの数値に対応したnビット誤りパターンテーブルを返す
	 * @param n
	 * @param max
	 * @return
	 */
	private static byte[] errorPatternTable(int n, int max) {
		byte[] table = new byte[max];
		int index = 0;
		int combNum;
		byte pastCombNum = 0;
		for(int i=0; i<n; i++) {
			combNum = combination(n, i);
			for(int j=0; j<combNum; j++) {
				table[index] = (byte) (countableCode(n, i, j) + pastCombNum);
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
	private static byte countableCode(int n, int k, int num) {
		Integer code = 0;
		int pascalNum;
		// パスカルの三角形上の座標
		while(num != 0) {
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
		return code.byteValue();
	}
	
	/**
	 * 底が２のlogの値を返す
	 * @param num
	 * @return
	 */
	private static double log2(double num) {
		return Math.log(num) / Math.log(2);
	}
	
	
	private static int factorial(int n) {
		return n <= 1 ? 1 : n * factorial(n-1);
	}
	
	
	private static int combination(int n, int k) {
		return factorial(n) / (factorial(k) * factorial(n-k));
	}
	
	
	private static int hamingWeight(int num) {
		int weight = 0;
		while(num != 0) {
			if(num%2 == 1) {
				weight++;
			}
			num /=2;
		}
		return weight;
	}

	private static String getRandomText(int textNum) {
		byte[] randByteArray = getRandomTextByte(textNum);
		String text = null;
		try {
			text =  new String(randByteArray, CHARACTER_CODE);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return text;
    }

	private static byte[] getRandomTextByte(int textNum) {
		Sfmt s = new Sfmt(1);
		Integer num;
		byte byte_num;
		byte[] randByteArray = new byte[textNum];
	
		for( int i=0; i<textNum; i++) {
		    // null文字などを避ける
		    num = s.NextInt(95) + 32;
		    byte_num = num.byteValue();
		    randByteArray[i] = byte_num;
		}
		return randByteArray;
    }
	
}
