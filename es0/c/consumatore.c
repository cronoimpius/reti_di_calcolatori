#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <stdbool.h>
#define MAX_STRING_LENGTH 256

// consumatore.c e' un filtro
int main(int argc, char* argv[]){

	char *file_in, read_char, *filter;//filter rappresenta la stringa di caratteri che vogliamo eliminare dal file
	int nread,i, fd,fil_len;//fil_len è la lunghezza della stringa di caratteri da eliminare
	bool match;
	
	//controllo numero argomenti
	if (argc == 3 ){
		file_in=argv[2];
		fd=open(file_in,O_RDONLY);
		if (fd<0){
                	perror("P0: Impossibile aprire il file.");
                	exit(2);
        	}
	}
	else if (argc ==2){ 
		fd=STDIN_FILENO;
	}else{
		perror(" numero di argomenti sbagliato"); 
		exit(1);
	} 
	
	filter = argv[1];
	fil_len=strlen(filter);
	
	while(nread = read(fd, &read_char, sizeof(char))) /* Fino ad EOF*/{
		if (nread >= 0){
			match=false;
			for (i=0; i<fil_len && !match; i++)
				if(read_char==filter[i])
					match=true;
				if(!match)
					putchar(read_char);
		}else{
			printf("(PID %d) impossibile leggere dal file %s", getpid(), file_in);
			perror("Errore!");
			close(fd);
			exit(3);
		}
	}
	close(fd);
}
