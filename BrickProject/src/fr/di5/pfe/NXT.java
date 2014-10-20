package fr.di5.pfe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.Battery;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.RConsole;

public class NXT {
	
	//private static int BrickNumber = 2; //pour la brick de gauche
	private static int BrickNumber = 1; //pour la brick de droite
	
	private static double espacementCase = 5.5;
	private static int downValue, upValue, closeValue, openValue;
	private static int armVal, pinceVal, xVal, yVal;
	
	//stream
	private static DataOutputStream dos;
	private static DataInputStream dis;

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterruptedException {
		
		try {
			//Initialisation du robot :
			initialisation();
		} catch(Exception e) {
			System.out.println("Erreur Initialisation");
			RConsole.println("erreur init");
		}
	
		while (true) {
			
	    	System.out.println("Waiting...");
	    	RConsole.println("wait connection");
			NXTConnection connection = Bluetooth.waitForConnection(); 
			
			//Initialisation des flux d'ecriture
			FileWrite fw = new FileWrite();
	    	
			System.out.println("Connected");
			
			dis = connection.openDataInputStream();
			dos = connection.openDataOutputStream();
			
			int i;
			try {
				while(true) {
					i = dis.readInt();
					System.out.println("Command : "+i);
			    	
					switch(i) {
						case 0: identification(); break;
						
						case 1: up(); break;
						case 2: down(); break;
						case 3: open(); break;
						case 4: close(); break;
						
						case 5: getAngle(Motor.B); break;
						case 6: getAngle(Motor.C); break;
						case 7: getAngle(Motor.A); break;
						
						case 16: moveXY( dis.readInt(), dis.readInt() ); break;
						case 17: moveY( dis.readInt(), dis.readInt() ); break;
					
						case 41: reset(); break; 
						case 42: dos.writeInt(Battery.getVoltageMilliVolt()); break;
						
						default: System.out.println("Command unknown : "+i); break;
					}
					
					dos.writeInt(i);
					dos.flush();

			    	try {
			    		fw.saveCoordinateInFile(armVal, pinceVal, xVal, yVal);
			    	}
			    	catch (Exception e) {
			    		System.out.println("Erreur sauvegarde fichier");
			    	}
				}
			} catch (IOException e) {
				System.out.println("Fin de Lecture");
			}
		
			System.out.println("Closing...");
			
			//On ferme les différents flux utiles
			try {
				
				dis.close();
			    dos.close();
		    	
			} catch (IOException e) {
				System.out.println("Erreur Fermeture des flux");
			}

	        connection.close();	
	        connection = null;
	        Thread.sleep(2000);
	    }
	}	
	
	public static void reset() {
		System.out.println("Reset");
				
		if(BrickNumber == 1) {
			moveXY(0, yVal);
			//On remet le compteur à zéro
			Motor.B.resetTachoCount();
		}
		
		//On remet le compteur à zéro
		Motor.A.resetTachoCount();
		
		xVal = 0;
		yVal = 0;
	}
	
	public static void initialisation() throws InterruptedException {
		System.out.println("Initialisation");
		
		FileRead rf = new FileRead();
		
		armVal = rf.getParameter(0);
		pinceVal = rf.getParameter(1);
		xVal = rf.getParameter(2);
		yVal = rf.getParameter(3);
		
		//si le bras était en position basse au moment de l'extinction
		downValue = (armVal == 600) ? 0 : 600;
		upValue = (armVal == 600) ? -600 : 0;
		closeValue = (pinceVal == 45) ? -45 : 0;
		openValue = (pinceVal == 45) ? 0 : 45;
	}
	
	public static void identification() throws IOException {
		dos.writeInt(BrickNumber);
	}
	
	public static void getAngle(NXTRegulatedMotor motor) throws IOException {
		if(BrickNumber == 2) {
			if(motor == Motor.B)
				dos.writeInt( pinceVal );
			else if(motor == Motor.C)
				dos.writeInt( armVal );
			else if(motor == Motor.A)
				dos.writeInt( yVal );
			else
				dos.writeInt(-1);
		}
		else {
			if(motor == Motor.B)
				dos.writeInt( xVal );
			else if(motor == Motor.A)
				dos.writeInt( yVal );
			else
				dos.writeInt(-1);
		}
	}

	public static void close() {
		Motor.B.setAcceleration(720);
		Motor.B.setSpeed(300); 
		Motor.B.rotateTo(closeValue, true);		
		pinceVal = 0;
	}
	
	public static void open() {
		Motor.B.setAcceleration(720);
		Motor.B.setSpeed(300);
		Motor.B.rotateTo(openValue, false);
		pinceVal = 45;
	}
	
	public static void down() {
		Motor.C.setAcceleration(720);
		Motor.C.setSpeed(500);
		Motor.C.rotateTo(downValue, false);
		armVal = 600;
	}
	
	public static void up() {
		Motor.C.setAcceleration(720);
		Motor.C.setSpeed(500);
		Motor.C.rotateTo(upValue, false);
		armVal = 0;
	}

	// on x : 38cm = -1000 -> 1cm = -1000/38
	// on y : 10cm = 275
	public static void moveXY(int x, int y) {
		//X et Y repr√©sente les coordonn√©es dans le tableau
		
		Motor.A.setAcceleration(720);
		Motor.A.setSpeed(300);
		Motor.B.setAcceleration(720);
		Motor.B.setSpeed(300);

		System.out.println( "x:"+x*espacementCase+"("+( x * 1000/38 *espacementCase )+"), y:"+y*espacementCase+"("+( y* 275/10 *espacementCase )+")" );
		
		Motor.A.rotateTo( (int)(y * 275/10 * espacementCase), false);
		Motor.B.rotateTo( (int)(x * 1000/38 * espacementCase), false );
		
		xVal = x;
		yVal = y;
	}
	
	public static void moveY(int x, int y) {
		//X et Y represente les coordonnées dans le tableau
		
		Motor.A.setAcceleration(720);
		Motor.A.setSpeed(300);		
		
		System.out.println( "x:"+x+"("+( x * 1000/38 )+"), y:"+y+"("+( y * 275/10 )+")" );
				
		Motor.A.rotateTo( (int)(y * 275/10 * espacementCase), false);
		
		xVal = x;
		yVal = y;
	}
}
