import java.io.*;

public class FileUtility {

	/**
	 * Nota: sorgente e destinazione devono essere correttamente aperti e chiusi
	 * da chi invoca questa funzione.
	 *
	 */
	static protected void trasferisci_a_byte_file_binario(DataInputStream src,
			DataOutputStream dest) throws IOException {

		// ciclo di lettura da sorgente e scrittura su destinazione
	    int buffer;
	    try {
	    	// esco dal ciclo all lettura di un valore negativo -> EOF
	    	// N.B.: la funzione consuma l'EOF
	    	while ((buffer=src.read()) >= 0) {
	    		dest.write(buffer);
	    	}
	    	dest.flush();
	    }
	    catch (EOFException e) {
	    	System.out.println("Problemi, i seguenti: ");
	    	e.printStackTrace();
	    }
	}

    static protected void trasferisci_N_byte_file_binario(DataInputStream src,
            DataOutputStream dest, long daTrasferire) throws
        IOException{
        int cont=0; //numero di ciclo di lettura / scrittura
        int buffer=0;
        try{
            //esco se ho raggiunto il numero di byte da trasferire
            while(cont<daTrasferire){
                buffer=src.read();
                dest.write(buffer);
                cont++;
            }
            dest.flush();

          //  System.out.println("Byte trasferiti: "+cont+"\n");
        }catch(EOFException e){
            System.out.println("Problemi, i seguenti: ");
            e.printStackTrace();
        }
    }

/*------------------------------------------------------------------------------------*/

    //ALGORITMO CHE TRASFERISCE PIÙ BYTE ALLA VOLTA IN BASE ALLA DIMENSIONE DEL FILE
    //NON FUNZIONA
    /*static protected void trasferisci_TOT_byte_file_binario(DataInputStream src,
            DataOutputStream dest, long daTrasferire) throws IOException{

        double count=0;
        int dim = (int) daTrasferire;

        //ricavo l'ordine di grandezza del file in migliaia
        while((dim=dim/1000) > 0)
        	count++;
        //se il file è piu piccolo di 1kb trasferisco un byte alla volta
        if(count == 0)
        	trasferisci_N_byte_file_binario(src, dest, daTrasferire);

        //se è piu grande trasferisco un 1kb, 1mb etc.
        else {
        	//mi preparo l'array che immagazzina i dati
        	byte[] trasfArray = new byte[(int) Math.pow(1000, count)];
        	System.out.println(Math.pow(1000, count));
        	int i = 0;
        	try{
        		int nCicli = (int)daTrasferire/trasfArray.length;

        		while(i < nCicli){
        			src.read(trasfArray, 0, trasfArray.length);
        			dest.write(trasfArray, 0, trasfArray.length);
        			i++;
        		}
        		dest.flush();

        		//se sono rimasti dei byte richiamo la funzione
        		if(daTrasferire%trasfArray.length != 0)
        			trasferisci_TOT_byte_file_binario(src, dest, daTrasferire-(nCicli*trasfArray.length));

          //  System.out.println("Byte trasferiti: "+cont+"\n");
        	}catch(EOFException e){
        		System.out.println("Problemi, i seguenti: ");
        		e.printStackTrace();
        	}
        }//else
}*/

/*------------------------------------------------------------------------------*/
    //ALGORITMO CHE TRASFERISCE 1KB ALLA VOLTA
    //PIÙ VELOCE MA ANCHE PIÙ DISPENDIOSO IN TERMINI DI RISORSE
    static protected void trasferisci_TOT_byte_file_binario(DataInputStream src,
            DataOutputStream dest, long daTrasferire) throws IOException{


        //se il file è più piccolo di un 1kb trasferisco byte per byte
        if((int)daTrasferire/1000 == 0)
        	trasferisci_N_byte_file_binario(src, dest, daTrasferire);
        //se è piu grande trasferisco un 1kb.
        else {
        	//mi preparo l'array che immagazzina i dati
        	byte[] trasfArray = new byte[1000];
        	int i = 0;
        	try{
        		int nCicli = (int)daTrasferire/1000;

        		while(i < nCicli){
        			src.read(trasfArray, 0, 1000);
        			dest.write(trasfArray, 0, 1000);
        			i++;
        		}
        		dest.flush();

        		//se sono rimasti dei byte richiamo la funzione
        		if(daTrasferire%1000 != 0)
        			trasferisci_TOT_byte_file_binario(src, dest, daTrasferire-(nCicli*1000));

          //  System.out.println("Byte trasferiti: "+cont+"\n");
        	}catch(EOFException e){
        		System.out.println("Problemi, i seguenti: ");
        		e.printStackTrace();
        	}
        }//else
}
}
