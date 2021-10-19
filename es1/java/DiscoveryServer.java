import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.StringTokenizer;

public class DiscoveryServer {

	// porta nel range consentito 1024-65535!
	// dichiarata come statica perch� caratterizza il server

	public static void main(String[] args) {

		System.out.println("DiscoveryServer: avviato");

		DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[256];
		byte[] data = new byte[256];
		int port = -1;
		int i=0,j=0;

		// controllo argomenti 
		if ((args.length < 3)) {
			System.out.println("Usage: java DiscoveryServer <port filename portFile>");
			System.exit(1);
		} 
		
		try {
			port = Integer.parseInt(args[0]);
			// controllo che la porta sia nel range consentito 1024-65535
			if (port < 1024 || port > 65535) {
				System.out.println("Usage: java LineServer [serverPort>1024]");
				System.exit(1);
				}
			} catch (NumberFormatException e) {
				System.out.println("Usage: java LineServer [serverPort>1024]");
				System.exit(1);
		} 

		//Controllo che tutte le porte sono diverse
		
		for ( i=2; i<args.length; i+=2){
                	for(j=i+2; j<=args.length; j+=2 ){
				if(args[i].equals(args[j])){
					System.out.println("Porta "+i+" e porta "+j+" sono uguali\n");
					System.exit(1);
				}
			}
			
		}

		/*int numS=(args.length-1)/2; //numero di SwapClient da creare

		String fileNames [numS]; //array contenente il nome dei file
		int ports [numS]; //array contenente il numero di porta

		for (i=0; i<numS; i++){ // ciclo per riempire l'array dei nomifile
			fileNames[i]=args[(i*2)+1];
		}

		for (i=0; i<=numS;i++){ //ciclo per riempire l'array delle porte
			ports[i]=args[i+2];
		}

		for (i=0; i<numS; i++){
			new RowSwapServer( fileNames[i],ports[i] ).start();
		}*/

		for ( i=1; i<args.length; i+=2){
			new RowSwapServer( args[i], args[i+1]).start();
		}

		try {
			socket = new DatagramSocket(port);
			packet = new DatagramPacket(buf, buf.length);
			System.out.println("Creata la socket: " + socket);
		}
		catch (SocketException e) {
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.exit(1);
		}

		try {
			String nomeFile = null;
			int ind = -1;
			String richiesta = null;
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
			StringTokenizer st = null;
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			String porta = "-1";
			//byte[] data = null;

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
					//esito="Error";
					// il server continua a fornire il servizio ricominciando dall'inizio  del ciclo
				}

				try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					richiesta = diStream.readUTF();
					st = new StringTokenizer(richiesta);
					nomeFile = st.nextToken(); //nome file passato dal cliente
					
				}
				catch (Exception e) {
					System.err.println("Problemi nella lettura della richiesta: "
						+ nomeFile);
					e.printStackTrace();
					continue;
					//esito="Error";
					// il server continua a fornire il servizio ricominciando dall'inizio del ciclo
				}

				// preparazione della linea e invio della risposta
				try {
					
					for(i=1; i<args.length-1 && ind<0; i+=2) {
						if(args[i].contentEquals(nomeFile)) {
							ind=i+1;
							porta=args[ind];
							}
					}
					if(ind < 0) {
						System.out.println("File name not found.");
						;
						//se il file non è presente nella lista passo alla prossima richiesta
					}
					
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(porta);
					data = boStream.toByteArray();
					packet.setData(data, 0, data.length);
					socket.send(packet);
				}
				catch (IOException e) {
					System.err.println("Problemi nell'invio della risposta: "
				      + e.getMessage());
					e.printStackTrace();
					continue;
					//esito="Error";
					// il server continua a fornire il servizio ricominciando dall'inizio del ciclo
				}

			} 

		}
		// qui catturo le eccezioni non catturate all'interno del while in seguito alle quali il server termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("LineServer: termino...");
		socket.close();
	}
}
