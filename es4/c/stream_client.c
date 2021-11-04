/* Client per richiedere l'invio di un file (get, versione 1) */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

//#define DIM_BUFF 100
#define LENGTH_FILE_NAME 4096

//uso un buffer di dimensione unitaria per leggere il risultato del server

int main(int argc, char *argv[]){
	int sd, nread, port;
	char buff, nome_dir[LENGTH_FILE_NAME];
	struct hostent *host;
	struct sockaddr_in servaddr;

	/* CONTROLLO ARGOMENTI ---------------------------------- */
	if(argc!=3){
		printf("Error:%s serverAddress serverPort\n", argv[0]);
		exit(1);
	}
	printf("Client avviato\n");

	/* PREPARAZIONE INDIRIZZO SERVER ----------------------------- */
	memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
	servaddr.sin_family = AF_INET;
	host = gethostbyname(argv[1]);
	if (host == NULL){
		printf("%s not found in /etc/hosts\n", argv[1]);
		exit(2);
	}

	nread = 0;
	while (argv[2][nread] != '\0'){
		if ((argv[2][nread] < '0') || (argv[2][nread] > '9')){
			printf("Secondo argomento non intero\n");
			exit(2);
		}
		nread++;
	}
	port = atoi(argv[2]);
	if (port < 1024 || port > 65535)
	{printf("Porta scorretta...");exit(2);}

	servaddr.sin_addr.s_addr=((struct in_addr*) (host->h_addr))->s_addr;
	servaddr.sin_port = htons(port);

	//creo la socket che permane fino alla file dell'esecuzione del client
	sd=socket(AF_INET, SOCK_STREAM, 0);
	if (sd <0){perror("apertura socket "); exit(3);}
	printf("Creata la socket sd=%d\n", sd);

	if (connect(sd,(struct sockaddr *) &servaddr, sizeof(struct sockaddr))<0)
	{perror("Errore in connect"); exit(4);}
	printf("Connect ok\n");

	/* CORPO DEL CLIENT: */
	/* ciclo di accettazione di richieste di file ------- */
	printf("Nome del direttorio da richiedere: ");

	while (gets(nome_dir)){

		if (write(sd, nome_dir, (strlen(nome_dir)+1))<0)
		{
			perror("write");
			printf("Nome del file da richiedere: ");
			continue;
		}

		printf("Richiesta dei file nei sotto-direttorii di %s inviata... \n", nome_dir);
		//continuo a leggere un byte alla volta finchè il server non mi manda '*'
		while(read(sd, &buff, 1)>0 && buff!='*')
		{
			/*se il carattere che leggo è un numero negativo
			interpreto il risulatato come un fallimento*/
			if(buff == 'N')
			{
				printf("Il direttorio passato non esiste.\n");
				continue;
			}
            else if (buff == '\0'){
                write(1,&buff,sizeof(char));
                printf("\n");
            }
			/*altrimento lo scrivo in output*/
			else
				write(1, &buff, 1);
		}
		printf("Nome del direttorio da richiedere: ");
	}//while

	/*prima di terminare il client chiudo la connessione*/
	printf("Chiudo connessione\n");
	shutdown(sd,1); shutdown(sd, 0);
	close(sd); //chiusura sempre DENTRO

	printf("\nClient: termino...\n");
	exit(0);
}
