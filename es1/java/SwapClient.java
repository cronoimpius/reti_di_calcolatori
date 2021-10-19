// LineClient.java

import java.io.*;
import java.net.*;

public class SwapClient {

	public static void main(String[] args) {

		InetAddress addr = null;
		int port=-1;
		int portRS= -1;
		
		//controllo argomenti
		try {
			if (args.length == 3) {
		    addr = InetAddress.getByName(args[0]);
		    port = Integer.parseInt(args[1]);
			} else {
				System.out.println("Usage: java SwapClients IPDS PortaDS fileName");
			    System.exit(1);
			}
		} catch (UnknownHostException e) {
			System.out
		      .println("Problemi nella determinazione dell'endpoint del server : ");
			e.printStackTrace();
			System.exit(2);
		}
		
		//iniz
		DatagramSocket socket = null;
		DatagramPacket packet = null;
		
		ByteArrayOutputStream boStream = null;
		DataOutputStream doStream = null;
		ByteArrayInputStream biStream = null;
		DataInputStream diStream = null;
		
		
		
		byte[] buf = new byte[256];
		byte[] data = new byte[264];

		// creazione della socket datagramDS e datagramRS, settaggio timeout di 30s
		// e creazione datagram packet
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(30000);
			packet = new DatagramPacket(buf, buf.length, addr, port);
			System.out.println("Creata la socket con il DS: " + socket);
			
			boStream = new ByteArrayOutputStream();
			doStream = new DataOutputStream(boStream);
			doStream.writeUTF(args[2]); //invio del nome del file
			packet.setData(boStream.toByteArray());
			socket.send(packet);
			System.out.println("Richiesta inviata a " + addr + ", " + port);
			
		} catch (IOException e) {
			System.out.println("Problemi nella creazione della socket o nell'invio del pacchetto: ");
			e.printStackTrace();
			System.exit(1);
		}

		try {
			String porta = null;
			packet.setData(data);
			socket.receive(packet);
			//recupero esito del DS
			biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
			diStream = new DataInputStream(biStream);
			porta = diStream.readUTF();
			System.out.println(porta);
			portRS = Integer.parseInt(porta);
			if(portRS < 0) {
				System.out.println("Errore nella richiesta della porta.\n");
				socket.close();
				System.exit(1);
			}
			
			//System.out.println("Porta: " + portRS);
			
		} catch (IOException e) {
			System.out.println("Problemi nella lettura della risposta: ");
			e.printStackTrace();
			System.exit(1);
		}		
		socket.close();
		
		// comunicazione con RowSwap
		String righe=null;
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		
		
		try {

			//apertura socket con RowSwapServer
			socket = new DatagramSocket();
			socket.setSoTimeout(30000);
			packet = new DatagramPacket(buf, buf.length, addr, portRS);
			
			System.out.println("Numero delle righe da scambiare? ");
			while ((righe=stdIn.readLine()) != null) {
				// interazione con l'utente e invio richiesta
				try {
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(righe);
					buf = boStream.toByteArray();
					packet.setData(buf);
					socket.send(packet);
					//System.out.print("Numero delle righe da scambiare? ");

					//System.out.println("Richiesta inviata a " + addr + ", " + portRS);
					
				} catch (Exception e) {
					System.out.println("Problemi nell'invio della richiesta: ");
					e.printStackTrace();
					System.out
				      .println("\n^D(Unix)/^Z(Win)+invio per uscire");
					continue;
				}

				try {
					//attesa risposta
					packet.setData(data);
					socket.receive(packet);
					
					//stampa esito
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					System.out.println(diStream.readUTF() + "\n");
					
				} catch (IOException e) {
					System.out.println("Problemi nella ricezione del datagramma: ");
					e.printStackTrace();
					System.out.println("\n^D(Unix)/^Z(Win)+invio per uscire");
					continue;
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}
				
			
				// tutto ok, pronto per nuova richiesta
				System.out.println("Numero delle righe da scambiare?");
				System.out.println("\n^D(Unix)/^Z(Win)+invio per uscire");
			}
			
			// while
		}
		
		// qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il client termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Client: termino...");
		socket.close();
	}
}