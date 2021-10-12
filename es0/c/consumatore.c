#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#define MAX_STRING_LENGTH 256

// consumatore.c e' un filtro
int main(int argc, char* argv[])
{
	char *ch, *file_in, read_char, buf[MAX_STRING_LENGTH];
	int nread, fd, match;
	
	//controllo numero argomenti
	if (argc != 3 && argc !=2)
	{ 
		perror(" numero di argomenti sbagliato"); exit(1);
	} 
	
	if(argc == 3)
	{
		file_in = argv[2];
		fd = open(file_in, O_RDONLY);
		if (fd<0){
			perror("P0: Impossibile aprire il file.");
			exit(2);
		}
	}
	else 
		fd=0;

	while(nread = read(fd, &read_char, sizeof(char))) /* Fino ad EOF*/
	{
		//ch=NULL;
		match=0;
		if (nread >= 0) 
		{
			/*ch=strstr(argv[1], &read_char);
			if(ch)
				match=1;*/
			for(int i = 0; argv[1][i]!='\0'; i++)
			{
            			if(read_char == argv[1][i])
					match=1;
			}
		}

		else
		{
			printf("(PID %d) impossibile leggere dal file %s", getpid(), file_in);
			perror("Errore!");
			close(fd);
			exit(3);
		}
		if(match==0) putchar(read_char);
	}
	close(fd);
}
