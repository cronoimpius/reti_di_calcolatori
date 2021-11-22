import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;

public class ServerImp extends UnicastRemoteObject implements RemOp{

	private static final long serialVersionUID = 1L;

	// Costruttore
		public ServerImp() throws RemoteException {
			super();
		}


		public int conta_righe(String nomeF, int numw)throws RemoteException{
        int res=0;
        BufferedReader buff;
        long start=0,end=0;//usato per controlli

        try{
            buff= new BufferedReader (new FileReader(nomeF));
        }catch (FileNotFoundException e){
            throw new RemoteException(e.toString());
        }
        System.out.println("Conto il numero di righe con almeno "+numw+" parole in "+nomeF +" \n");
        String line=null;
        start=System.currentTimeMillis();
        try{
            while((line = buff.readLine())!=null){
                String[] word = line.split("\\s+|,\\s*|\\.\\s*");
                if(word.length>numw && word!=null)
                    res++;
            }
        }catch(IOException e){
            throw  new RemoteException(e.toString());
        }
        end=System.currentTimeMillis();
        System.out.println("---Tempo impiegato server "+(end-start)+"---\n");
        try{
            buff.close();
        }catch (IOException e){
            throw new RemoteException(e.toString());
        }
        return res;

    }

    public String elimina_riga(String nomeF, int nl)throws RemoteException{
        int att=1; //il file inizia a contare dalla linea 1
        String newN = nomeF.substring(0,(nomeF.length()-4))+"_mod.txt";
        String res=null;
        long start=0,end=0;
        File temp = new File(newN);

        BufferedWriter out=null;
        try{
            out= new BufferedWriter (new FileWriter(temp));
        }catch(IOException e){
            throw new RemoteException(e.toString());
        }
        BufferedReader in=null;

        try {
            in = new BufferedReader (new FileReader(nomeF));
        }catch (FileNotFoundException e){
            throw new RemoteException(e.toString());
        }

        System.out.println("Elimino la riga numero "+nl+" da "+ nomeF);

        String line=null;
        start=System.currentTimeMillis();
        try{
		    while((line=in.readLine())!=null){
                if( att != nl) //se la linea non è quella da eliminare la scrivo
                    out.write(line+'\n');
                att++;
            }

            in.close();
            out.close();
        }catch (IOException e){
            throw new RemoteException(e.toString());
        }
        end=System.currentTimeMillis();
        System.out.println("---Tempo impiegato server "+(end-start)+" ---\n");
        if (att< nl){
            throw new RemoteException("Il file remoto ha "+att+" righe, deve averne almeno "+nl);
        }else{
            res = newN+" "+Integer.toString(att-2);
        }

        return res;
    }

 // Avvio del Server RMI
 	public static void main(String[] args) {


 		int REGISTRYPORT = 1099;
 		String registryHost = "localhost";
 		String serviceName = "ServerImp";		//lookup name...

        //controllo parametri
        if(args.length != 0 && args.length !=1){
            System.out.println("Sintassi : ServerImp [REGISTRYPORT]");
            System.exit(1);
        }
        if(args.length == 1){
            try{
                REGISTRYPORT= Integer.parseInt(args[0]);
            }catch (Exception e){
                System.out.println("Sintassi: ServerImp [REGISTRYPORT]");
                System.exit(2);
            }
        }
 		// Registrazione del servizio RMI
 		String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/"
 			    + serviceName;
 		try{
            //System.setProperty("java.rmi.server.hostname","127.0.0.1");
 			//Registry registry=LocateRegistry.createRegistry(REGISTRYPORT);
            ServerImp serverRMI = new ServerImp();
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
