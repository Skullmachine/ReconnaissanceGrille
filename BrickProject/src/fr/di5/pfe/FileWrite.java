package fr.di5.pfe;

import java.io.*;

public class FileWrite {

	public static String filename = "rollingbridge.dat";
	
	private FileOutputStream out = null;
	private DataOutputStream dos = null;
	private File data;

	
	public FileWrite() {
	    data = new File(filename);
	    
		try {	
			
			data.delete();
			data.createNewFile();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveCoordinateInFile(int armAngle, int pinceAngle, int yAngle, int xAngle) throws IOException {
		    out = new FileOutputStream(data);
		    dos = new DataOutputStream(out);
		   
		    String chaine = armAngle+":"+pinceAngle+":"+xAngle+":"+yAngle+":";
		    
	    	dos.write(chaine.getBytes());
	    	dos.flush();
	    	
			dos.close();
	    	out.close();
	    	
	}
}