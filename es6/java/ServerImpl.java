


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerImpl extends UnicastRemoteObject implements RemOp {
	
	private static final long serialVersionUID = 1L;

	// Costruttore
	public ServerImpl() throws RemoteException {
		super();
	}
	
	public int conta_righe(String nomeFile, int parole) throws RemoteException{
		 int res=0;
		 BufferedReader buff;
	        try{
	        	buff= new BufferedReader (new FileReader(nomeFile));
	        }catch (FileNotFoundException e){
	            throw new RemoteException(e.toString());
	        }
	        System.out.println("Conto il numero di righe con almeno "+
	        				parole+" parole in "+nomeFile +" \n");
	        String line;
	        try {
	        	while((line = buff.readLine())!=null){
	        		if(!line.isEmpty()) {
	        			String[] word = line.split("\\s+|,\\s*|\\.\\s*");
		        		System.out.println(word.length);
		        		if(word.length>=parole)
		        			res++;
	        		}
	   	        }

	   	        buff.close();
	        }catch(IOException e) {
	        	throw new RemoteException(e.toString());
	        }

	        return res;
	}
	
	public synchronized  String elimina_riga(String nomeFile, int riga)
			throws RemoteException{
		int att=1;
		String res;//il file inizia a contare dalla linea 1
        String newN = nomeFile.substring(0,(nomeFile.length()-4))+"_modified.txt";
        File temp = new File(newN);
        BufferedWriter out;
        BufferedReader in;
        try {
        	out= new BufferedWriter (new FileWriter(temp));
            in = new BufferedReader (new FileReader(nomeFile));
        }catch (IOException e){
            throw new RemoteException(e.toString());
        }

        System.out.println("Elimino la riga numero "+riga+" da "+ nomeFile);

        String line;

        try {
            while((line=in.readLine())!=null){
                if( att != riga)  //se la linea non quella da eliminare la scrivo
                    out.write(line+"\n");
                att++;
            }
            in.close();
            out.close();
        }catch(IOException e) {
        	throw new RemoteException(e.toString());
        }
        
        if (att< riga){
            throw new RemoteException();
        }else{
        	res= newN +" " +(att-2);
        	System.out.println(res);
        }

        return res;
	}	

	// Avvio del Server RMI
	public static void main(String[] args) {

		final int REGISTRYPORT = 1099;
		String registryHost = "localhost";
		String serviceName = "RemOp";		//lookup name...

		// Registrazione del servizio RMI
		String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/"
				+ serviceName;
		try{
			ServerImpl serverRMI = new ServerImpl();
			Naming.rebind(completeName, serverRMI);
			System.out.println("Server RMI: Servizio \"" + serviceName
					+ "\" registrato");
		}
		catch(Exception e){
			System.err.println("Server RMI \"" + serviceName + "\": "
					+ e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}