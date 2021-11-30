/*
    Interfaccia remota Tag Registry per il Client
    CercaTag= Cerca i Server con quel tag
*/

import java.rmi.RemoteException;

public interface RegistryRemotoTagClient extends RegistryRemotoClient{
    public String [] cercaTag(String[] tag) throws RemoteException;
}
