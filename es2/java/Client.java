import java.net.*;
import java.io.*;

public class Client {

	public static void main(String[] args) throws IOException {

		InetAddress addr = null;
		int port = -1;

		try{ //check args
			if(args.length == 2){
				addr = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
			} else{
				System.out.println("Usage: java PutFileClient serverAddr serverPort dimFile");
				System.exit(1);
			}
		} //try
		catch(Exception e){
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out.println("Usage: java PutFileClient serverAddr serverPort");
			System.exit(2);
		}

		// oggetti utilizzati dal client per la comunicazione e la lettura del file
		// locale
		Socket socket = null;
		FileInputStream inFile = null;
		DataInputStream inSock = null;
		DataOutputStream outSock = null;
		File cartella=null;
		String dir=null;

		// creazione socket
		try{
			socket = new Socket(addr, port);
			socket.setSoTimeout(60000);
			System.out.println("Creata la socket: " + socket);
		}
		catch(Exception e){
			System.err.println("Errore creazione socket");
			e.printStackTrace();
			System.err.println("Chiudo!");
			System.exit(3);
	    }

		// creazione stream di input/output su socket
		try{
			inSock = new DataInputStream(socket.getInputStream());
			outSock = new DataOutputStream(socket.getOutputStream());
		}
		catch(IOException e){
			System.out
				.println("Problemi nella creazione degli stream su socket: ");
			e.printStackTrace();
			System.out
				.print("\n^D(Unix)/^Z(Win)+invio per uscire");
		}

		// creazione stream di input da tastiera
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		System.out
		    .print("Client Started.\n\n^D(Unix)/^Z(Win)+invio per uscire");

		System.out.print("\n\nInserire directory:");

		while((dir=stdIn.readLine())!=null) {
			cartella=new File(dir);
			int dim=-1;

			if(!cartella.isDirectory()){
				System.out.println("L'argomento passato non esiste o non una directory.");
				System.out.println("Inserisci una directory:(Crlt+D per terminare)");
				continue;
			}

			System.out.println("Inserisci la minima dimensione di file:");
			try {
				dim = Integer.parseInt(stdIn.readLine());
			}
			catch(NumberFormatException e){
				System.out.println("Inserisci un intero!");
				socket.close();
				System.exit(1);
			}


			//lavoro su ogni file
			for(File f : cartella.listFiles()) {

				if(f.length()< dim || !f.isFile())
					continue;
				else {
					// trasmissione del nome
					try{
						outSock.writeUTF(f.getName());
						System.out.println("Inviato il nome del file " + f.getName());
					}
					catch(Exception e){
						System.out.println("Problemi nell'invio del nome di " + f.getName()
							+ ": ");
						e.printStackTrace();
						System.out
					      	.print("\n^D(Unix)/^Z(Win)+invio per uscire");
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
						continue;
					}

					// ricezione esito
					String esito;
					try{
						esito = inSock.readUTF();
						if(esito.equalsIgnoreCase("attiva")) {
							// trasmissione della dim del file
							try{
								outSock.writeLong(f.length());
								System.out.println("Inviata la dimensione del file " + f.getName());
							}
							catch(Exception e){
								System.out.println("Problemi nell'invio della dimensione del file " + f.getName()
									+ ": ");
								e.printStackTrace();
								System.out
								.print("\n^D(Unix)/^Z(Win)+invio per uscire");
									// il client continua l'esecuzione riprendendo dall'inizio del ciclo
								continue;
							}

							// trasferimento file
							try{
								System.out.println("Inizio la trasmissione di " + f.getName());
								inFile= new FileInputStream(f.getAbsolutePath());
								FileUtility.trasferisci_N_byte_file_binario(new DataInputStream(inFile), outSock, f.length());
								inFile.close();
								System.out.println("Trasmissione di " + f.getName() + " terminata, file salvato sul server\n ");

							}
							catch(SocketTimeoutException ste){
								System.out.println("Timeout trasmissione file scattato: ");
								ste.printStackTrace();
								socket.close();
								System.out
									.print("\n^D(Unix)/^Z(Win)+invio per uscire\n");
								// il client continua l'esecuzione riprendendo dall'inizio del ciclo
								continue;
							}
							catch(Exception e){
								System.out.println("\nProblemi nell'invio di " + f.getName() + ": ");
								e.printStackTrace();
								socket.close();
								System.out
								.print("\n^D(Unix)/^Z(Win)+invio per uscire");
								// il client continua l'esecuzione riprendendo dall'inizio del ciclo
								continue;
							}
						}else if(esito.equalsIgnoreCase("salta file")) {
							System.out.println(f.getName() + " esistente\n");
							continue;
						}
						else {
							System.out.println("Stringa esito non valida\n");
							continue;
						}

					}
					catch(SocketTimeoutException ste){
						System.out.println("Timeout scattato: ");
						ste.printStackTrace();
						socket.close();
						System.out
							.print("\n^D(Unix)/^Z(Win)+invio per uscire");
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
						continue;
					}
					catch(Exception e){
						System.out
							.println("Problemi nella ricezione dell'esito, i seguenti: ");
						e.printStackTrace();
						socket.close();
						System.out
					      	.print("\n^D(Unix)/^Z(Win)+invio per uscire");
						continue;
					} //fine try catch con controllo esito

				}	//fine else
			}//fine for
			System.out.println("Inserisci una directory:(Crlt+D per terminare)");

		}

		try {
			System.out.println("Connessione terminata.");
			stdIn.close();
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}//fine main
}//fine classe


