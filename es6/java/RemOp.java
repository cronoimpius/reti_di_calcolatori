import java.io.IOException;
import java.rmi.*;

public interface RemOp extends Remote{

	public int conta_righe(String nomeF, int numw)throws RemoteException, IOException;
	public String elimina_riga(String nomeF, int nl)throws RemoteException, IOException;
}
