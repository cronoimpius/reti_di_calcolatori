import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;

public class ServerImpC extends UnicastRemoteObject implements RemOp {
	
	private static final long serialVersionUID=1L;

	// Costruttore
		public ServerImpC() throws RemoteException {
			super();
		}


		public int conta_righe(String nomeF, int numw)throws RemoteException{
        int res=0,i=0,wc=0;
        BufferedReader buff;
        char ch;
        long start=0,end=0;//usato per controlli

        try{
            buff= new BufferedReader (new FileReader(nomeF));
        }catch (FileNotFoundException e){
            throw new RemoteException(e.toString());
        }
        System.out.println("Conto il numero di righe con almeno "+numw+" parole in "+nomeF +" \n");

        start=System.currentTimeMillis();
        try{
            while((i=buff.read())>=0){
                ch=(char) i;
                if(ch=='\n'){           //trovo il newline
                    if(wc>=numw) res++;  //se il numero di parole contate supera il minimo aumento
                    wc=0;               //setto a zero il counter delle parole per iniziare una nuova riga
                }
                if(ch==' ' || ch== ',' || ch=='.' || ch=='\n' || ch==':' ){ // aggiungere altri separatori di parole
                    wc++;
                }
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
        int att=1,i; //il file inizia a contare dalla linea 1
        String newN = nomeF.substring(0,(nomeF.length()-4))+"_mod.txt";
        String res;
        char ch;
        long start=0,end=0;
        File temp = new File(newN);

        BufferedWriter out= new BufferedWriter(new FileWriter(temp));
        BufferedReader in=null;

        try {
            in = new BufferedReader (new FileReader(nomeF));
        }catch (FileNotFoundException e){
            throw new RemoteException(e.toString());
        }

        System.out.println("Elimino la riga numero "+nl+" da "+ nomeF);

        start=System.currentTimeMillis();
        try{
		    while((i=in.read())>=0){
                ch=(char) i;

                if(ch!='\n'){
                    if(att!=nl)
                        out.write(ch);
                }
                else if(ch =='\n'){
                    if(att!=nl)
                        out.write('\n');
                    att++;
                }
            }

            in.close();
            out.close();
            if (att<nl){
                throw new RemoteException("Il file remoto ho "+att+" righe, deve averne almento "+nl);
            }else{
                res=newN+" "+Integer.toString(att);
            }
        }catch (IOException e){
            throw new RemoteException(e.toString());
        }
        end=System.currentTimeMillis();
        System.out.println("---Tempo impiegato server "+(end-start)+" ---\n");

        return res;
    }

 // Avvio del Server RMI
 	public static void main(String[] args) {


 		int REGISTRYPORT = 1099;
 		String registryHost = "localhost";
 		String serviceName = "ServerImpC";		//lookup name...

        //controllo parametri
        if(args.length != 0 && args.length !=1){
            System.out.println("Sintassi : ServerImpC [REGISTRYPORT]");
            System.exit(1);
        }
        if(args.length == 1){
            try{
                REGISTRYPORT= Integer.parseInt(args[0]);
            }catch (Exception e){
                System.out.println("Sintassi: ServerImpC [REGISTRYPORT]");
                System.exit(2);
            }
        }
 		// Registrazione del servizio RMI
 		String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/"
 			    + serviceName;
 		try{
            //System.setProperty("java.rmi.server.hostname","127.0.0.1");
 			//Registry registry=LocateRegistry.createRegistry(REGISTRYPORT);
            ServerImpC serverRMI = new ServerImpC();
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
