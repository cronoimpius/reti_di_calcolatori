import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Consumatore {
	public static void main(String[] args) {
		FileReader r = null;
		char ch;
		int x;
		
		if (args.length != 2 && args.length!=1){
			System.out.println("Utilizzo: consumatore <inputFilename>");
			System.exit(0);
		}
	  
		try {
			if(args.length==2)
				r = new FileReader(args[1]);
			else if(args.length==1)
				r = new FileReader(FileDescriptor.in);
			
		} catch(FileNotFoundException e){
			System.out.println("File non trovato");
			System.exit(1);
		}
		
		try {
			while ((x = r.read()) >= 0) { 
				ch = (char) x;
				if(!args[0].contains(ch+""))
					System.out.print(ch);
			}
			r.close();
			
		} catch(IOException ex){
			System.out.println("Errore di input");
			System.exit(2);
		}
}}
