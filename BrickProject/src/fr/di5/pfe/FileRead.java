package fr.di5.pfe;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileRead {

	File data;
	private int pinceAngle, armAngle, xAngle, yAngle;
	
	public FileRead() {
	    data = new File(FileWrite.filename);
	    
	    pinceAngle = 0; armAngle = 0; xAngle = 0; yAngle = 0;
	    
	    if(data.exists())
	    	readFile();
	}
	
	public void readFile() {
	    try {
	    	
		      InputStream is = new FileInputStream(data);
		      DataInputStream din = new DataInputStream(is);
		           
		      byte[] byteArray = new byte[32];
		      String result = "";
		      
		      if(din.read(byteArray) != -1)
		    	  result = new String(byteArray);
		      
		      int idParameter = 0;
		      int indexFirst=0, indexSecond;
		          
		      //on recupere les infos de la chaine
		      while((indexSecond = result.indexOf(":", indexFirst)) != -1) {
		    	
		    	  switch(idParameter) {
		    	  	case 0: armAngle = Integer.parseInt(result.substring(indexFirst, indexSecond));  System.out.println("Bras : "+armAngle); break;
		    	  	case 1: pinceAngle = Integer.parseInt(result.substring(indexFirst, indexSecond));  System.out.println("Pince : "+pinceAngle);break;
		    	  	case 2: xAngle = Integer.parseInt(result.substring(indexFirst, indexSecond));  System.out.println("x : "+xAngle);break;
		    	  	case 3: yAngle = Integer.parseInt(result.substring(indexFirst, indexSecond));  System.out.println("y : "+yAngle); break;
		    	  }
		    	  
		    	  indexFirst = indexSecond+1;
		    	  idParameter++;
		      }
		      
		      din.close();
	    } catch(IOException e) {
	    	System.err.println("Read Exception");
	    	System.exit(1);
	    }
	}
	
	public int getParameter(int parameter) {
		switch(parameter) {
		  	case 0: 
		  		return armAngle;
		  	case 1: 
		  		return pinceAngle; 
		  	case 2: 
		  		return xAngle; 
		  	case 3: 
		  		return yAngle;
		  	default:
		  		return -1;
		}
	}
}
