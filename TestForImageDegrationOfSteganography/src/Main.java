import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageIO;

public class Main {
	private static final String IMAGE_PATH = "./img/";
	private static final String CHARACTER_CODE = "US-ASCII";
	private static final int TEXT_LENGTH = 256;
	
	public static void main(String[] args) {
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
