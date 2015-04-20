import java.util.Map;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.ColorConvertOp;
import java.nio.ByteBuffer;

/*
 * @test
 * @bug 6862970
 * @summary something with icc. MAybe passing always
 *
 * @run main/othervm ICCTestIBM
 */


public class ICCTestIBM {
	private static class ICCTag {
		int name;
		int off;
		int size;

		public ICCTag(int name, int offset, int size) {
			this.name = name;
			this.off = offset;
			this.size = size;
		}

//		public String toString() {
//			return "Tag(" + name + "," + offset + "," + size + ")";
//		}
	}

	private static final int PROFILE = ColorSpace.CS_LINEAR_RGB;
//	private static final int NEW_TAG = ICC_Profile.icSigAToB0Tag;
	static byte[] k;

	public static void main(String[] args) {

		int option = 3;
		try {
		option = Integer.parseInt(args[0]);
		}
		catch (Exception e) {
			option = 3;
		}
		switch (option) {
			
		case 3:
			test_ColorConvertOp();
			break;
		default:
			usage();
			break;
		} 
	}

	private static void usage() {
		System.out.println("ConvertColorOp crash while usage of large ICC_Profile");
	}



	private static void test_ColorConvertOp() {
		ICC_Profile orig = ICC_Profile.getInstance(PROFILE);
		byte[] origData = orig.getData();

		ICCTag[] tags = getTags(orig);
		int i, max = 0;
		for (i = 0; i < tags.length; ++i) {
			if (tags[i].size > max)
				max = tags[i].size;
		}
		System.out.println(max); 
		//max *=  ;
		if (max > 150 * 1024 * 1024) {
			max = 150 * 1024 * 1024; 
		}
		byte[] newData = new byte[max];
		for (i = 0; i < max; ++i) {
			newData[i] = (byte) 'p';

		}
		System.out.println("Tags.length="+tags.length ); 
		for (i = 1; i < tags.length; ++i) {
			System.out.print(tags[i].name + ": replacing " + tags[i].size
					+ " bytes " + "at tag index " + i + " with "
					+ newData.length + " bytes: ");
			System.out.println("i:"+i+":"+tags[i].size );
		//	if (tags[i].size < 90) { // isolated crash case
				ICC_Profile pf = ICC_Profile.getInstance(origData);

				try {
				pf.setData(tags[i].name, newData);

				ColorConvertOp cco = new ColorConvertOp(
						new ICC_Profile[] { pf }, new RenderingHints(null));
				ICC_Profile[] pf2 = cco.getICC_Profiles() ;
				
				BufferedImage bimage = new BufferedImage(1000,1000,
						BufferedImage.TYPE_BYTE_BINARY     );
				System.out.println("before filter"); 
				BufferedImage cimage = cco.filter(bimage, bimage);
				Raster t = cimage.getData(); 
				WritableRaster u = cimage.getRaster();
				bimage = cco.filter(cimage, bimage);
				u = cco.filter(t, u);
				System.out.println(bimage.getPropertyNames()); 
				System.out.println("after filter");
				}
				catch (Throwable e) {
					e.printStackTrace(); 
				}

		//	}

 
		}
		System.out.print("Tags.length="+tags.length ); 
	}

	static private ICCTag[] getTags(ICC_Profile pf) {
		// getTags from ICC_Profile
		ByteBuffer bb = ByteBuffer.wrap(pf.getData());
		byte[] hdr = new byte[128];
		bb.get(hdr);
		int len = bb.getInt();
		ICCTag[] tags = new ICCTag[len*10];
		for (int i = 0; i < len*10; ++i)
			try {
			tags[i] = new ICCTag(bb.getInt(), bb.getInt(), bb.getInt());
			}
			catch (Exception e) {
			tags[i] = new ICCTag(Integer.MAX_VALUE-i, 5+i, 23000);
			}
		
		return tags;
	}
}
