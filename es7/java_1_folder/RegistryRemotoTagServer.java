/*
    Interfaccia remota Registry per i Server usando i tag, estende quella per
    il client.
    associaTag = Associa il nome logico di un server ad un tag
*/

import java.rmi.RemoteException;

public interface RegistryRemotoTagServer extends RegistryRemotoServer,RegistryRemotoTagClient{
    public boolean associaTag( String nomeLogico, String[] tag ) throws RemoteException;
}
