import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

class Client {

	public static void main(String[] args) {
		final int REGISTRYPORT = 1099;
        long start=0, end=0; //usati per fare misure
	    String registryHost = null;					//host remoto con registry
	    //String serviceName = "ServerCongresso";		//lookup name...Hardcoded
	    String serviceName = "";
	    BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

		// Controllo dei parametri della riga di comando
		if(args.length != 2){
			System.out.println("Sintassi: RMI_Registry_IP ServiceName");
			System.exit(1);
		}
		registryHost = args[0];
		serviceName = args[1];

		System.out.println("Invio richieste a "+registryHost+" per il servizio di nome "+serviceName);

		// Connessione al servizio RMI remoto
		try{
			String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/"
					+ serviceName;
			RemOp serverRMI = (RemOp) Naming.lookup(completeName);
			System.out.println("ClientRMI: Servizio \"" + serviceName + "\" connesso");

			System.out.println("\nRichieste di servizio fino a fine file");

			String service;
			System.out.print("Servizio (C=Conta righe di un file , E=elimina righe di un file): ");

			/*ciclo accettazione richieste utente*/
			while ((service = stdIn.readLine()) != null){

				if(service.equals("C")){

					int parole = 0;
					System.out.print("Nome del file?");
					String fileName = stdIn.readLine();
					System.out.print("Numero di parole minimo per riga? ");
					parole = Integer.parseInt(stdIn.readLine());

                    start = System.currentTimeMillis();
                    try{
					    //if(serverRMI.conta_righe(fileName, parole)>0)
						System.out.println("Sono state trovate "+serverRMI.conta_righe(fileName, parole)
						+ " righe con piu di "+parole+" parole nel file "+fileName);

					    //else System.out.println("Nessuna riga che rispetti le specifiche e' stata trovata");
                    }catch (RemoteException re){
                        System.out.println("Errore remoto: "+re.toString());
                    }
                    end = System.currentTimeMillis();
                    System.out.println("---Tempo impiegato client "+(end-start)+" millisecondi---\n");
				} // C=conta righe

				else if(service.equals("E")){
					int riga = 0;
					boolean ok = false;
					System.out.print("Nome del file?");
					String fileName = stdIn.readLine();

					System.out.print("Riga da eliminare? ");
					while (ok != true){
						// TODO: check NumberFormatException
						riga = Integer.parseInt(stdIn.readLine());
						if(riga <=0){
							System.out.println("Riga non valida");
							System.out.print("Riga da eliminare? ");
							continue;
						} else ok = true;
					}
                    start=System.currentTimeMillis();
                    try{
					    //if(serverRMI.elimina_riga(fileName,riga)!=null){
						String[] rec=serverRMI.elimina_riga(fileName,riga).split(" ");
                            /*
                                la prima parte della stringa indica il nome del nuovo
                                file, mentre la seconda il numero di righe presenti
                                nel nuovo file
                            */
                        System.out.println("Nuovo file "+rec[0]+" con "+rec[1]+" righe\n");
                        //}
					    //else System.out.println("Operazione fallita\n");
                    }catch(RemoteException re) {
                        System.out.println("Errore remoto: "+re.toString());
                    }
                    end=System.currentTimeMillis();
                    System.out.println("--Tempo impiegato client "+(end-start)+" millisecondi---\n");
				} // E=Elimina riga

				else System.out.println("Servizio non disponibile");

				System.out.print("Servizio (C=Conta righe di un file , E=elimina righe di un file): ");
			} // while (!EOF), fine richieste utente

		}
		catch(NotBoundException nbe){
			System.err.println("ClientRMI: il nome fornito non risulta registrato; " + nbe.getMessage());
			//nbe.printStackTrace();
			System.exit(1);
		}
		catch(Exception e){
			System.err.println("ClientRMI: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
