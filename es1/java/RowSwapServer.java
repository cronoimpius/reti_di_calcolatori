import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.StringTokenizer;

public class RowSwapServer extends Thread {
	
	private String filename=null;
	private int port=-1;
	
	public RowSwapServer(String filename, String port) {
		try{
			this.filename = filename;
			this.port = Integer.parseInt(port);
			
		}catch(NumberFormatException e) {
			System.out.println("Porta non valida passata al costruttore\n ");
		}
	}

	public void run() {
		//creazione socket e datagramPacket
		String esito=null;
		DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[256];
		
		try {
			socket = new DatagramSocket(port);
			packet = new DatagramPacket(buf, buf.length);
			System.out.println("Creata la socket con Client: " + socket);
		}
		catch (SocketException e) {
			System.out.println("Problemi nella creazione della socket");
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			int numriga1=-1;
			int numriga2=-1;
			String richiesta = null;
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
			StringTokenizer st = null;
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			byte[] data = null;

			while (true) {
				System.out.println("\nIn attesa di richieste...");
				
				// ricezione del datagramma
				try {
					packet.setData(buf);
					socket.receive(packet);
				}
				catch (IOException e) {
					System.err.println("Problemi nella ricezione del datagramma: "
							+ e.getMessage());
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio  del ciclo
				}

				try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					richiesta = diStream.readUTF();
					st = new StringTokenizer(richiesta);
					numriga1 = Integer.parseInt(st.nextToken()); //nome file passato dal cliente
					numriga2 = Integer.parseInt(st.nextToken());
					
				}
				catch (Exception e) {
					System.err.println("Problemi nella lettura della richiesta");
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio del ciclo
				}
				
				long startTime = System.currentTimeMillis();

				if(this.swap(filename, numriga1, numriga2)) {
					esito="Scambio avvenuto con successo!\n";
				} else esito="Errore nello scambio\n";

				long endTime = System.currentTimeMillis();
				long elapsedTime = endTime - startTime;
				System.out.println("Tempo di esecuzione della richiesta:" + elapsedTime);
					
				// preparazione della linea e invio della risposta
				try {
					
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(esito);
					data = boStream.toByteArray();
					packet.setData(data, 0, data.length);
					socket.send(packet);
				}
				catch (IOException e) {
					System.err.println("Problemi nell'invio della risposta: "
				      + e.getMessage());
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio del ciclo
				}

			} // while

		}
		// qui catturo le eccezioni non catturate all'interno del while in seguito alle quali il server termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("RowSwapServer: termino...");
		socket.close();
		
	}
	
	
	private boolean swap(String fileIn,int rig1,int rig2)throws IOException,FileNotFoundException {
		
		if(rig1==rig2 || rig1 <= 0 || rig2 <= 0) {//invalid arguments
			return false;
		}
		
		BufferedReader read=new BufferedReader(new FileReader(fileIn));
		int n=1;
		String riga=null;
		
		String r1=null;
		String r2=null;
		
		while(((riga=read.readLine()) != null) || (r1==null && r2==null)) { //se trovo r1 e r2 esco dal ciclo
			if(n==rig1) {
				r1=riga;
			}else if(n==rig2) {
				r2=riga;
			}
			n++;
		}
		read.close();
		
		read = new BufferedReader(new FileReader(fileIn));
		n=1;
		StringBuilder s=new StringBuilder();
		if(r1==null || r2==null) {
			return false;
		}else {
			while((riga = read.readLine()) != null) {
				if(n == rig1) {
					s.append(r2 + "\n");
				}else if(n == rig2) {
					s.append(r1 + "\n");
				}else {
					s.append(riga + "\n");
				}
				n++;
			}
			read.close();
			BufferedWriter write = new BufferedWriter(new FileWriter(fileIn));
			write.write(s.toString());
			write.close();
		}
		return true;
	}
	
}
