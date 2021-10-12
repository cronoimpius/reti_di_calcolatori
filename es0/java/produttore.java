import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class Produttore {
	public static void main(String[] args) {
		//long startTime = System.currentTimeMillis();
		BufferedReader in = null;
		
		if (args.length != 1){
			System.out.println("Utilizzo: produttore <inputFilename>");
			System.exit(0);
		}
		
		in= new BufferedReader(new InputStreamReader(System.in));
		FileWriter fout;
		String inputl = null;
		try {
			fout = new FileWriter(args[0]);
			System.out.println("Inserisci la nuova riga");
			
			while((inputl=in.readLine())!=null){
				System.out.println("Inserisci la nuova riga");
				inputl = inputl+"\n";
				fout.write(inputl, 0, inputl.length());	
			}
			
			/*do{
				System.out.println("Inserisci la nuova riga");
				inputl = in.readLine()+"\n";
				if(!inputl.startsWith("EOF"))
					fout.write(inputl, 0, inputl.length());	
			}while(!inputl.startsWith("EOF"));*/
			
			fout.close();
		} 
		catch (NumberFormatException nfe) { 
			nfe.printStackTrace(); 
			System.exit(1); // uscita con errore, intero positivo a livello di sistema Unix
		}
	    catch (IOException e) { 
			e.printStackTrace();
			System.exit(2); // uscita con errore, intero positivo a livello di sistema Unix
		}
		/*long endTime = System.currentTimeMillis();
		long timeElapsed = endTime-startTime;
		System.out.println("Tempo impiegato:"+timeElapsed);*/
	}
}

