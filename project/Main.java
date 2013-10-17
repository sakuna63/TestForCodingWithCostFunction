public class Main {
    public static void main(String[] args) {
	String text = getRandomText(256);
	System.out.println( text );
	/*byte[] a = getRandomTextByte(256);
	for (int i=0; i<a.length; i++) {
	    System.out.println( a[i]);
	}
	*/
    }

    private static String getRandomText(int textNum) {
	byte[] randByteArray = getRandomTextByte(textNum);
	String text = null;
	try {
	    text =  new String(randByteArray, "US-ASCII");
	}
	catch(Exception e) {
	    System.out.println(e.getMessage());
	    //	    e.printStackTrace();
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
