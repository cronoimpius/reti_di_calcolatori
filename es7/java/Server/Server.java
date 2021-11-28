/**
 * Server.java
 * 		Implementazione del server
 * */

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Server extends UnicastRemoteObject implements ServerInt{
  /*static Programma prog[]*/;

  //private String nome;
  // Costruttore
  public Server() throws RemoteException {
    super();
    //this.nome=name;
  }
/*
  // Richiede una prenotazione
  public int registrazione(int giorno, String sessione, String speaker)
      throws RemoteException {
    int numSess = -1;
    System.out.println("Server RMI: richiesta registrazione con parametri");
    System.out.println("giorno   = " + giorno);
    System.out.println("sessione = " + sessione);
    System.out.println("speaker  = " + speaker);

    if (sessione.startsWith("S")) {
      try {
        numSess = Integer.parseInt(sessione.substring(1)) - 1;
      } catch (NumberFormatException e) {
      }
    }

    // Se i dati sono sbagliati significa che sono stati trasmessi male e quindi
    // solleva una eccezione
    if (numSess == -1)
      throw new RemoteException();
    if (giorno < 1 || giorno > 3)
      throw new RemoteException();

    return prog[giorno - 1].registra(numSess, speaker);
  }

  // Ritorno il campo
  public Programma programma(int giorno) throws RemoteException {
    System.out.println("Server RMI: richiesto programma del giorno " + giorno);
    return prog[giorno - 1];
  }
*/

  // Avvio del Server RMI
  public static void main(String[] args) {

    // creazione programma
/*    prog = new Programma[3];
    for (int i = 0; i < 3; i++)
      prog[i] = new Programma();
*/
    int registryRemotoPort = 1099;
    String registryRemotoName = "RegistryRemoto";
    String serviceName = ""; // "ServerCongresso";
    String[] tag=null;
    boolean rp=false;// per vedere se c'è la porta o meno

    // Controllo dei parametri della riga di comando
    if (args.length <2) {
      System.out
          .println("Sintassi: ServerCongressoImpl NomeHostRegistryRemoto [registryPort] almeno un tag");
      System.exit(1);
    }
    String registryRemotoHost = args[0];
    try {
        registryRemotoPort = Integer.parseInt(args[1]);
        rp=true;
    }catch (Exception e) {
        rp=false;
        /*System.out
            .println("Sintassi: ServerCongressoImpl NomeHostRegistryRemoto [registryPort], registryPort intero tag");
        System.exit(2);*/
    }
    if(rp && args.length>=3){
        tag=new String[args.length-2];
        for(int i=2;i<args.length;i++)
            tag[i-2]=args[i];
    }
    else if(args.length>=2 && !rp){
        tag=new String[args.length-1];
        for(int i=1; i<args.length;i++)
            tag[i-1]=args[i];
    }

    serviceName="Server"+tag[0];
    System.out.println(serviceName+"\n");
    // Impostazione del SecurityManager
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new RMISecurityManager());
    }

    // Registrazione del servizio RMI
    String completeRemoteRegistryName = "//" + registryRemotoHost + ":"
        + registryRemotoPort + "/" + registryRemotoName;

    try {
      RegistryRemotoTagServer registryRemoto = (RegistryRemotoTagServer) Naming
          .lookup(completeRemoteRegistryName);
      Server serverRMI = new Server();
      registryRemoto.aggiungi(serviceName, serverRMI);
      //associo il tag al nome logico
      registryRemoto.associaTag(serviceName,tag);
      System.out.println("Server RMI: Servizio \"" + serviceName
          + "\" registrato");
    } catch (Exception e) {
      System.err.println("Server RMI \"" + serviceName + "\": "
          + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
}
