// PutFileServer Concorrente

import java.io.*;
import java.net.*;

// Thread lanciato per ogni richiesta accettata
// versione per il trasferimento di file binari
class ServerConThread extends Thread{

	private Socket clientSocket = null;

	/**
	 * Constructor
	 * @param clientSocket
	 */
	public ServerConThread(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		DataInputStream inSock;
		DataOutputStream outSock;
		long d=0;//DIM FILE
		try {
			String nomeFile = null;
			FileOutputStream outFile = null;
			try {
				// creazione stream di input e out da socket
				inSock = new DataInputStream(clientSocket.getInputStream());
				outSock = new DataOutputStream(clientSocket.getOutputStream());

			}
			catch(SocketTimeoutException ste){
				System.out.println("Timeout scattato: ");
				ste.printStackTrace();
				clientSocket.close();
				System.out
					.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
				return;
			}
			catch (IOException ioe) {
				System.out
					.println("Problemi nella creazione degli stream di input/output "
							+ "su socket: ");
				ioe.printStackTrace();
				// il server continua l'esecuzione riprendendo dall'inizio del ciclo
				return;
			}
			catch (Exception e) {
				System.out
					.println("Problemi nella creazione degli stream di input/output "
							+ "su socket: ");
				e.printStackTrace();
				return;
			}
			try {//prendere i file e vedere se esistono
				while(true){ /*(nomeFile=inSock.readUTF())!=null*/
					nomeFile=inSock.readUTF();
                    System.out.println("Ricevo il file ");
					File f=new File(nomeFile);
					if(f.exists()) {
						outSock.writeUTF("salta file");
                        System.out.println("File presente nel server\n");
					}else {
						outSock.writeUTF("attiva");
                        d=inSock.readLong();
                        System.out.println("Ricevo il file "+nomeFile+" di dim "+d+"\n");
                        outFile = new FileOutputStream(nomeFile);
                        FileUtility.trasferisci_N_byte_file_binario(inSock, new DataOutputStream(outFile),d);
                        outFile.close();
					}

					System.out.println("\nRicezione del file " + nomeFile + " terminata\n");
				}

			}catch(EOFException eof){
                System.out.println("Fine file, chiudo...\n");
                clientSocket.close();
                System.out.println("ServerThread terminato...\n");
                return;
            }catch(SocketTimeoutException ste){
				System.out.println("Timeout scattato: ");
				ste.printStackTrace();
				clientSocket.close();
				System.exit(1);
			}
			catch (Exception e) {
				System.err
					.println("Problemi, seguenti \n");
				e.printStackTrace();
				clientSocket.close();
				System.exit(2);
			}
		}
	    // qui catturo le eccezioni non catturate all'interno del while
	    // in seguito alle quali il server termina l'esecuzione
	    catch (IOException ioe) {
	    	ioe.printStackTrace();
	    	System.out
	          .println("Errore irreversibile, PutFileServerThread: termino...");
	    	System.exit(3);
	    }
	} // run
} // PutFileServerThread class

public class PutServerCon {
	public static final int PORT = 1050; //default port

	public static void main(String[] args) throws IOException {

		int port = -1;

		/* controllo argomenti */
	    try {
	    	if (args.length == 1) {
	    		port = Integer.parseInt(args[0]);
	    		if (port < 1024 || port > 65535) {
	    			System.out.println("Usage: java LineServer [serverPort>1024]");
	    			System.exit(1);
	    		}
	    	} else if (args.length == 0) {
	    		port = PORT;
	    	} else {
	    		System.out
	    			.println("Usage: java PutFileServerThread or java PutFileServerThread port");
	    		System.exit(1);
	    	}
	    } //try
	    catch (Exception e) {
	    	System.out.println("Problemi, i seguenti: ");
	    	e.printStackTrace();
	    	System.out
	          	.println("Usage: java PutFileServerThread or java PutFileServerThread port");
	    	System.exit(1);
	    }

	    ServerSocket serverSocket = null;
	    Socket clientSocket = null;

	    try {
	    	serverSocket = new ServerSocket(port);
	    	serverSocket.setReuseAddress(true);
	    	System.out.println("PutFileServerCon: avviato ");
	    	System.out.println("Server: creata la server socket: " + serverSocket);
	    }
	    catch (Exception e) {
	    	System.err
	    		.println("Server: problemi nella creazione della server socket: "
	    				+ e.getMessage());
	    	e.printStackTrace();
            serverSocket.close();
	    	System.exit(1);
	    }

	    try {

	    	while (true) {
	    		System.out.println("Server: in attesa di richieste...\n");

	    		try {
	    			// bloccante fino ad una pervenuta connessione
	    			clientSocket = serverSocket.accept();
	    			//clientSocket.setSoTimeout(30000);
	    			System.out.println("Server: connessione accettata: " + clientSocket);
	    		}
	    		catch (Exception e) {
	    			System.err
	    				.println("Server: problemi nella accettazione della connessione: "
	    						+ e.getMessage());
	    			e.printStackTrace();
	    			continue;
	    		}

	    		// serizio delegato ad un nuovo thread
	    		try {
	    			new ServerConThread(clientSocket).start();
	    		}
	    		catch (Exception e) {
	    			System.err.println("Server: problemi nel server thread: "
	    					+ e.getMessage());
	    			e.printStackTrace();
	    			continue;
	    		}

	    	} // while
	    }
	    // qui catturo le eccezioni non catturate all'interno del while
	    // in seguito alle quali il server termina l'esecuzione
	    catch (Exception e) {
	    	e.printStackTrace();
	    	// chiusura di stream e socket
	    	System.out.println("PutServerCon: termino...");
	    	System.exit(2);
	    }

	}
} // PutFileServerCon class
