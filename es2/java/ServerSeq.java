// PutFileServer Sequenziale
import java.io.*;
import java.net.*;

public class ServerSeq {
	public static final int PORT = 54321; // porta default per server

	public static void main(String[] args) throws IOException {
		// Porta sulla quale ascolta il server
		int port = -1;

		/* controllo argomenti */
		try {
			if (args.length == 1) {
				port = Integer.parseInt(args[0]);
				// controllo che la porta sia nel range consentito 1024-65535
				if (port < 1024 || port > 65535) {
					System.out.println("Usage: java PutFileServerSeq or java PutFileServerSeq port");
					System.exit(1);
				}
			} else if (args.length == 0) {
				port = PORT;
			} else {
				System.out
					.println("Usage: java PutFileServerSeq or java PutFileServerSeq port");
				System.exit(1);
			}
		} //try
		catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out
				.println("Usage: java PutFileServerSeq or java PutFileServerSeq port");
			System.exit(1);
		}

		/* preparazione socket e in/out stream */
		ServerSocket serverSocket = null;
		try {
//			serverSocket = new ServerSocket(port,2);
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			System.out.println("ServerSeq: avviato ");
			System.out.println("Creata la server socket: " + serverSocket);
		}
		catch (Exception e) {
			System.err.println("Problemi nella creazione della server socket: "
					+ e.getMessage());
			e.printStackTrace();
			System.exit(2);
		}
		try {
			//ciclo infinito server
			while (true) {
				Socket clientSocket = null;
				DataInputStream inSock = null;
				DataOutputStream outSock = null;

				System.out.println("\nIn attesa di richieste...");
				try {
					clientSocket = serverSocket.accept();
					clientSocket.setSoTimeout(30000); //timeout altrimenti server sequenziale si sospende
					System.out.println("Connessione accettata\n");
				}
				catch (SocketTimeoutException te) {
					System.err
						.println("Non ho ricevuto nulla dal client per 30 sec., interrompo "
								+ "la comunicazione e accetto nuove richieste.");
					// il server continua a fornire il servizio ricominciando dall'inizio
					continue;
				}
				catch (Exception e) {
					System.err.println("Problemi nella accettazione della connessione: "
							+ e.getMessage());
					e.printStackTrace();
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo, se ci sono stati problemi
					continue;
				}

				//stream I/O e ricezione nome file
				String nomeFile;
				try {
					inSock = new DataInputStream(clientSocket.getInputStream());
					outSock = new DataOutputStream(clientSocket.getOutputStream());
		        }
				catch(SocketTimeoutException ste){
					System.out.println("Timeout scattato: ");
					ste.printStackTrace();
					clientSocket.close();
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
					continue;
				}
				catch (IOException e) {
		        	System.out
		        		.println("Problemi nella creazione degli stream di input/output "
		        			+ "su socket: ");
		        	e.printStackTrace();
		        	continue;
		        }

				//elaborazione e comunicazione esito
				//FileOutputStream outFile = null;
				try {
					while(true) {
						nomeFile = inSock.readUTF();
						String esito;
						File curFile = new File(nomeFile);
						// controllo su file
						if (curFile.exists()) {
							try {
								esito = "Salta File";
	                            System.out.println(nomeFile + " esistente\n");
	                            outSock.writeUTF(esito);
							}
							catch (Exception e) {
								System.out.println("Problemi nella notifica di file esistente: ");
								e.printStackTrace();
								continue;
							}
						}
						else {
							esito = "Attiva";
		                    outSock.writeUTF(esito);
		                  //ricezione file
		    				try {
		                        long sizeF=inSock.readLong();//prendo l'intero
		    					FileOutputStream outFile = new FileOutputStream(nomeFile);
		                        System.out.println("Ricevo il file " + nomeFile + " di dimensione "+sizeF);
		    					long start = System.currentTimeMillis();
		    					FileUtility.trasferisci_N_byte_file_binario(inSock,
		    							new DataOutputStream(outFile),sizeF);
                                long end =System.currentTimeMillis();
                                System.out.println("--Tempo impiegato "+(end-start)+" --\n");
		    					System.out.println("Ricezione del file " + nomeFile
		    							+ " terminata\n");
		    					outFile.close();

		    				}
		    				catch(SocketTimeoutException ste){
		    					System.out.println("Timeout scattato: ");
		    					ste.printStackTrace();
		    					clientSocket.close();
		    					System.out
		    						.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
		    					continue;
		    				}
		    				catch (Exception e) {
		    					System.err
		    						.println("\nProblemi durante la ricezione e scrittura del file: "
		    								+ e.getMessage());
		    					e.printStackTrace();
		    					clientSocket.close();
		    					System.out.println("Terminata connessione con " + clientSocket);
		    					continue;
		    				}
						}

					}//fine while interno

				}catch(EOFException e){
                    clientSocket.shutdownInput();
                    outSock.flush();
                    clientSocket.shutdownOutput();
					System.out.println("Il cliente non ha nulla da mandare, passo al prossimo cliente\n");
					continue;
				}

			} // while (true)
		}
		catch (Exception e) {
			e.printStackTrace();
			// chiusura di stream e socket
			System.out.println("Errore irreversibile, PutFileServerSeq: termino...");
			System.exit(3);
		}
	} // main
}
