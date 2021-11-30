/**
 * 	Implementazione del Registry Remoto.
 *	Metodi descritti nelle interfacce.
 */

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RegistryRemotoImpl extends UnicastRemoteObject implements
	RegistryRemotoTagServer {

	private static final String [] tags={
        "Medico", "Bar", "Dentista","Supermercato","Ristorante","Congresso"
    };
    //lista dei tag consentiti
    // num. entry [nomelogico][ref][tag]
	final int tableSize = 100;

	// Tabella: la prima colonna contiene i nomi, la seconda i riferimenti remoti e la terza il tag relativo
	Object[][] table = new Object[tableSize][3];

    //costruttore
	public RegistryRemotoImpl() throws RemoteException {
		super();
		for (int i = 0; i < tableSize; i++) {
			table[i][0] = null;
			table[i][1] = null;
            table[i][2] = null;
		}
	}

	/** Aggiunge la coppia nella prima posizione disponibile */
	public synchronized boolean aggiungi(String nomeLogico, Remote riferimento)
			throws RemoteException {
		// Cerco la prima posizione libera e la riempio
		boolean risultato = false;
		if( (nomeLogico == null) || (riferimento == null) )
			return risultato;
		for (int i = 0; i < tableSize; i++)
			if (table[i][0] == null) {
				table[i][0] = nomeLogico;
				table[i][1] = riferimento;
				risultato = true;
				break;
			}
		return risultato;
	}

  /** Restituisce il riferimento remoto cercato, oppure null */
	public synchronized Remote cerca(String nomeLogico) throws RemoteException {
		Remote risultato = null;
		if( nomeLogico == null ) return null;
		for (int i = 0; i < tableSize; i++)
			if ( nomeLogico.equals((String) table[i][0]) ) {
				risultato = (Remote) table[i][1];
				break;
			}
		return risultato;
	}

	/** Restituisce tutti i riferimenti corrispondenti ad un nome logico */
	public synchronized Remote[] cercaTutti(String nomeLogico)
			throws RemoteException {
		int cont = 0;
		if( nomeLogico == null ) return new Remote[0];
		for (int i = 0; i < tableSize; i++)
			if ( nomeLogico.equals((String) table[i][0]) )
				cont++;
		Remote[] risultato = new Remote[cont];
		// Ora lo uso come indice per il riempimento
		cont = 0;
		for (int i = 0; i < tableSize; i++)
			if ( nomeLogico.equals((String) table[i][0]) )
				risultato[cont++] = (Remote) table[i][1];
		return risultato;
	}

	/** Restituisce tutti i riferimenti corrispondenti ad un nome logico */
	public synchronized Object[][] restituisciTutti() throws RemoteException {
		int cont = 0;
		for (int i = 0; i < tableSize; i++)
			if (table[i][0] != null) cont++;
		Object[][] risultato = new Object[cont][2];
		// Ora lo uso come indice per il riempimento
		cont = 0;
		for (int i = 0; i < tableSize; i++)
			if (table[i][0] != null) {
				risultato[cont][0] = table[i][0];
				risultato[cont][1] = table[i][1];
			}
		return risultato;
	}

	/** Elimina la prima entry corrispondente al nome logico indicato */
	public synchronized boolean eliminaPrimo(String nomeLogico)
			throws RemoteException {
			boolean risultato = false;
			if( nomeLogico == null ) return risultato;
			for (int i = 0; i < tableSize; i++)
				if ( nomeLogico.equals((String) table[i][0]) ) {
					table[i][0] = null;
					table[i][1] = null;
					risultato = true;
					break;
				}
			return risultato;
	}

	public synchronized boolean eliminaTutti(String nomeLogico)
			throws RemoteException {
		boolean risultato = false;
		if( nomeLogico == null ) return risultato;
		for (int i = 0; i < tableSize; i++)
			if ( nomeLogico.equals((String) table[i][0]) ) {
				if (risultato == false)
        	risultato = true;
				table[i][0] = null;
				table[i][1] = null;
			}
		return risultato;
	}

    // restituisco tutti i riferimenti che contengono il tag specificato
    public synchronized String[] cercaTag (String[] tag) throws RemoteException{
        int dim = 0; // dimensione della lista restituita, ovvero il numero di servizi con il tag specificato
        String[] res =null; // lista che restituisco alla fine

        // inizio a contare quanti servizi hanno il tag specificato
        for(int i=0; i<tableSize && table[i][0]!=null; i++){ // Se table[i][0] == null non è un Servizio
            int find=0; // conto i tag che fanno match
            String[] tmp = (String[])table[i][2]; //estraggo tutti i tag relativi al servizio
            for (int j=0; j<tmp.length;j++){// scorro i tag relativi al servizio
                for (int k=0; k<tag.length;k++){
                    if(tmp[j].equals(tag[k]))
                        find++;
                }
            }
            if(find==tag.length){
                dim++;
            }
        }
        if (dim==0)
            throw new RemoteException("Nessuna corrispondenza trovata");

        res=new String[dim];// creo la lista della dimensione giusta
        dim=0; // per riusare la variabile

        for (int i=0; i<tableSize && table[i][0]!=null; i++){
            int find=0;
            String[] tmp = (String[])table[i][2];
            for (int j=0;j<tmp.length;j++){
                for (int k=0;k<tag.length;k++){
                    if(tmp[j].equals(tag[k]))
                        find++;
                }
            }
            if(find==tag.length){
                res[dim]=(String)table[i][0];// salvo il nome logico nella lista
                dim++;
            }
        }
        return res;
    }

    // associo i tag a nomi logici del servizio
    public synchronized boolean associaTag(String nomeLogico, String[] tag)throws RemoteException{

        boolean res=false;
        // controllo che il tag sia presente tra quelli che posso scegliere
        for(String temp : tag){ //scorro i tag passati in ingresso
            for (String reg : tags){ //scorro i tag tra cui posso scegliere
                if(temp.equals(reg))
                    res=true;
            }
        }
        if(res=false)
            throw new RemoteException("Tag non valido");
        // essendo sicuro che il tag esista, associo il tag al Servizio
        res=false;
        if((nomeLogico==null) || (tag==null))
            return res;
        for(int i=0; i<tableSize;i++){
            if(nomeLogico.equals((String) table[i][0])){
                table[i][2] = tag;
                res=true;
            }
        }
        return res;
    }

	// Avvio del Server RMI
	public static void main(String[] args) {

		int registryRemotoPort = 1099;
		String registryRemotoHost = "localhost";
		String registryRemotoName = "RegistryRemoto";

		// Controllo dei parametri della riga di comando
		if (args.length != 0 && args.length != 1) {
			System.out.println("Sintassi: ServerImpl [registryPort]");
			System.exit(1);
		}
		if (args.length == 1) {
			try {
				registryRemotoPort = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out
				.println("Sintassi: ServerImpl [registryPort], registryPort intero");
				System.exit(2);
			}
		}

		// Impostazione del SecurityManager
		if (System.getSecurityManager() == null)
			System.setSecurityManager(new RMISecurityManager());

		// Registrazione del servizio RMI
		String completeName = "//" + registryRemotoHost + ":" + registryRemotoPort
				+ "/" + registryRemotoName;
		try {
			RegistryRemotoImpl serverRMI = new RegistryRemotoImpl();
			Naming.rebind(completeName, serverRMI);
			System.out.println("Server RMI: Servizio \"" + registryRemotoName
					+ "\" registrato");
		} catch (Exception e) {
			System.err.println("Server RMI \"" + registryRemotoName + "\": "
					+ e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
