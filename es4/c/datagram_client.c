/* Client per richiedere il numero di file in un direttorio remoto */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <string.h>
#include <dirent.h>
#include <fcntl.h>

#define LENGTH 30

typedef struct{
    char nome[LENGTH];
    char p[LENGTH];
}Request;

int main(int argc, char **argv){
        Request req;
	struct hostent *host;
	struct sockaddr_in clientaddr, servaddr;
	int sd, nread, port,ris;

	int len;
	char nome_file[LENGTH];
        char parola[LENGTH];

	/* CONTROLLO ARGOMENTI ---------------------------------- */
	if(argc!=3){
		printf("Error:%s serverAddress serverPort\n", argv[0]);
		exit(1);
	}


	/* INIZIALIZZAZIONE INDIRIZZO SERVER--------------------- */
	memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
	servaddr.sin_family = AF_INET;
	host = gethostbyname (argv[1]);
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

	servaddr.sin_addr.s_addr=((struct in_addr *)(host->h_addr))->s_addr;
	servaddr.sin_port = htons(port);

	/* INIZIALIZZAZIONE INDIRIZZO CLIENT--------------------- */
	memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));
	clientaddr.sin_family = AF_INET;
	clientaddr.sin_addr.s_addr = INADDR_ANY;
	clientaddr.sin_port = 0;

	printf("Client avviato\n");

	/* CREAZIONE SOCKET ---------------------------- */
	sd=socket(AF_INET, SOCK_DGRAM, 0);
	if(sd<0) {perror("apertura socket"); exit(3);}
	printf("Creata la socket sd=%d\n", sd);

	/* BIND SOCKET, a una porta scelta dal sistema --------------- */
	if(bind(sd,(struct sockaddr *) &clientaddr, sizeof(clientaddr))<0)
	{perror("bind socket "); exit(1);}
	printf("Client: bind socket ok, alla porta %i\n", clientaddr.sin_port);

	/* CORPO DEL CLIENT: */
	printf("Nome del file: ");

	while (gets(nome_file)){
                strcpy(req.nome,nome_file);
                printf("nome: %s\n",req.nome);
                printf("Parola da eliminare: ");
                gets(parola);
                strcpy(req.p,parola);
                printf("parola: %s\n",req.p);

                /* invio richiesta */
		len=sizeof(servaddr);

                if (sendto(sd, &req, sizeof(req), 0, (struct sockaddr *)&servaddr, len)<0){
			perror("sendto");
			continue; // se questo invio fallisce il client torna all'inzio del ciclo
		}

		/* ricezione del risultato */
		printf("Attesa del risultato...\n");
		if (recvfrom(sd, &ris, sizeof(ris), 0, (struct sockaddr *)&servaddr, &len)<0){
			perror("recvfrom");
			continue; // se questa ricezione fallisce il client torna all'inzio del ciclo
		}

                //ris=ntohl(ris);

        if (ris<0) printf("Il file %s è scorretto o non esiste\n", nome_file);

		else printf("Nel file %s sono state eliminate %d parole\n",nome_file,ris);

		printf("Nome del file: ");

	} // while

	printf("\nClient: termino...\n");
	close(sd);
	exit(0);
}
