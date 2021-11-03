/* Client per richiedere l'ordinamento remoto di un file */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <sys/time.h>

int main(int argc, char *argv[])
{
	int sd, port, fd_file, nread, nLine;
	char buff;

	// FILENAME_MAX: lunghezza massima nome file. Costante di sistema.
	char nome_file[FILENAME_MAX+1], inputNum[FILENAME_MAX+1];
	struct hostent *host;
	struct sockaddr_in servaddr;

    //per il testing
    struct timeval t1,t2;
    double trascorso;

	/* CONTROLLO ARGOMENTI ---------------------------------- */
	if(argc!=3){
		printf("Error:%s serverAddress serverPort\n", argv[0]);
		exit(1);
	}

	/* INIZIALIZZAZIONE INDIRIZZO SERVER -------------------------- */
	memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
	servaddr.sin_family = AF_INET;
	host = gethostbyname(argv[1]);

	/*VERIFICA INTERO*/
	nread=0;
	while( argv[2][nread]!= '\0' ){
		if( (argv[2][nread] < '0') || (argv[2][nread] > '9') ){
			printf("Secondo argomento non intero\n");
			exit(2);
		}
		nread++;
	}
	port = atoi(argv[2]);

	/* VERIFICA PORT e HOST */
	if (port < 1024 || port > 65535){
		printf("%s = porta scorretta...\n", argv[2]);
		exit(2);
	}
	if (host == NULL){
		printf("%s not found in /etc/hosts\n", argv[1]);
		exit(2);
	}else{
		servaddr.sin_addr.s_addr=((struct in_addr *)(host->h_addr))->s_addr;
		servaddr.sin_port = htons(port);
	}

	/* CORPO DEL CLIENT:
	ciclo di accettazione di richieste da utente ------- */
	printf("Richiesta di eliminare una linea di un file\n");
	printf("Nome del file, EOF per terminare: ");

	//inizio ciclo client
	while (gets(nome_file)){
		printf("File da aprire: __%s__\n", nome_file);

		/* Verifico l'esistenza del file */
		if((fd_file=open(nome_file, O_RDONLY))<0)
		{
			perror("open file sorgente");
			printf("Qualsiasi tasto per procedere, EOF per fine: ");
			continue;
		}

		//controllo il numero di linea
		nLine=0; nread=0;
        printf("Inserisci la riga da eliminare: ");
		gets(inputNum);
		while(inputNum[nread]!='\0' && nLine==0)
		{
			if(inputNum[nread]<'0' || inputNum[nread]>'9')
				nLine=-1;
			nread++;
		}
		if(nLine<0)
		{
			printf("Il numero di riga deve essere essere un intero positivo!\n");
			close(fd_file);
			printf("Qualsiasi tasto per procedere, EOF per fine: ");
			continue;
		}
		if((nLine = atoi(inputNum)) == 0)
		{
			printf("Il numero di riga deve essere essere diverso da 0!\n");
			close(fd_file);
			printf("Qualsiasi tasto per procedere, EOF per fine: ");
			continue;
		}

		/* CREAZIONE SOCKET ------------------------------------ */
		sd=socket(AF_INET, SOCK_STREAM, 0);
		if(sd<0) {perror("apertura socket"); exit(1);}
		printf("Client: creata la socket sd=%d\n", sd);

		/* Operazione di BIND implicita nella connect */
		if(connect(sd,(struct sockaddr *) &servaddr, sizeof(struct sockaddr))<0)
		{perror("connect"); exit(1);}
		printf("Client: connect ok\n");

		//invio numero di linea
		printf("Client:invio il numero di linea...\n");
		if(nread=write(sd, &nLine, sizeof(int))<=0)
		{
			printf("Errore nell'invio della riga!\n");
			close(fd_file); close(sd);
			printf("Nome del file da ordinare, EOF per terminare: ");
			continue;
		}

		/*INVIO File*/
        printf("Client: invio file\n");

        gettimeofday(&t1,NULL);

        while(nread = read(fd_file, &buff, 1) != 0){
			write(sd, &buff, 1);
		}

        gettimeofday(&t2,NULL);
        trascorso=(t2.tv_sec-t1.tv_sec)*1000.0;

		printf("Client: file inviato in %f ms\n",trascorso);
		//chiusura ed eliminazione file, chiusura canale in scrittura
		close(fd_file);
		unlink(nome_file);

		shutdown(sd, 1);

		/*Creo il file in scrittura*/
		if((fd_file=open(nome_file, O_WRONLY|O_CREAT, 0644))<0)
		{
			perror("open file destinatario");
			close(sd);
			printf("Nome del file da ordinare, EOF per terminare: ");
			continue;
		}

		/*RICEZIONE File*/
		printf("Client: ricevo e stampo file ordinato\n");

        gettimeofday(&t1,NULL);

        while((nread=read(sd,&buff,1))>0)
		{
			write(fd_file, &buff, 1);
			//write(1, &buff, 1);
		}

        gettimeofday(&t2,NULL);
        trascorso=(t2.tv_sec-t1.tv_sec)*1000.0;
		printf("Trasferimento terminato in %f ms\n",trascorso);

		/* Chiusura socket in ricezione */
		shutdown(sd, 0);
		/* Chiusura file */
		close(fd_file);
		close(sd);

		printf("Nome del file da ordinare, EOF per terminare: ");
	}//while
	printf("\nClient: termino...\n");
	exit(0);
}

