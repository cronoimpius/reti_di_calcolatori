/* Server che riceve un file e lo ridirige ordinato al client */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <errno.h>
#include <fcntl.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <sys/time.h>

#define LENGTH 256

/********************************************************/
void handler(int signo){
  int state;
  printf("esecuzione handler SIGCHLD\n");
  wait(&state);
}
/********************************************************/

int main(int argc, char **argv)
{
	int  listen_sd, conn_sd,nread;
	int port, len, numLine,lcount;
    char car, error[LENGTH];
	const int on = 1;
	struct sockaddr_in cliaddr, servaddr;
	struct hostent *host;

    //per il testing
    struct timeval t1,t2;
    double trascorso;

	/* CONTROLLO ARGOMENTI ---------------------------------- */
	if(argc!=2){
		printf("Error: %s port\n", argv[0]);
		exit(1);
	}
	else{
		nread=0;
		while( argv[1][nread]!= '\0' ){
			if( (argv[1][nread] < '0') || (argv[1][nread] > '9') ){
				printf("Primo argomento non intero\n");
				exit(2);
			}
			nread++;
		}
		port = atoi(argv[1]);
		if (port < 1024 || port > 65535){
			printf("Error: %s port\n", argv[0]);
			printf("1024 <= port <= 65535\n");
			exit(2);
		}

	}

	/* INIZIALIZZAZIONE INDIRIZZO SERVER ----------------------------------------- */
	memset ((char *)&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = INADDR_ANY;
	servaddr.sin_port = htons(port);

	/* CREAZIONE E SETTAGGI SOCKET D'ASCOLTO --------------------------------------- */
	listen_sd=socket(AF_INET, SOCK_STREAM, 0);
	if(listen_sd <0)
	{perror("creazione socket "); exit(1);}
	printf("Server: creata la socket d'ascolto per le richieste di eliminazione, fd=%d\n", listen_sd);

	if(setsockopt(listen_sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on))<0)
	{perror("set opzioni socket d'ascolto"); exit(1);}
	printf("Server: set opzioni socket d'ascolto ok\n");

	if(bind(listen_sd,(struct sockaddr *) &servaddr, sizeof(servaddr))<0)
	{perror("bind socket d'ascolto"); exit(1);}
	printf("Server: bind socket d'ascolto ok\n");

	if (listen(listen_sd, 5)<0) //creazione coda d'ascolto
	{perror("listen"); exit(1);}
	printf("Server: listen ok\n");

	/* AGGANCIO GESTORE PER EVITARE FIGLI ZOMBIE,
	* Quali altre primitive potrei usare? E' portabile su tutti i sistemi?
	* Pregi/Difetti?
	* Alcune risposte le potete trovare nel materiale aggiuntivo!
	*/
	signal(SIGCHLD, handler);

	/* CICLO DI RICEZIONE RICHIESTE --------------------------------------------- */
	for(;;){
	  	len=sizeof(cliaddr);
		if((conn_sd=accept(listen_sd,(struct sockaddr *)&cliaddr,&len))<0){
		/* La accept puo' essere interrotta dai segnali inviati dai figli alla loro
		* teminazione. Tale situazione va gestita opportunamente. Vedere nel man a cosa
		* corrisponde la costante EINTR!*/
			if (errno==EINTR){
				perror("Forzo la continuazione della accept");
				continue;
			}
			else exit(1);
		}

		if (fork()==0){ // figlio
			/*Chiusura FileDescr non utilizzati e ridirezione STDIN/STDOUT*/
			close(listen_sd);
			host=gethostbyaddr( (char *) &cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
			if (host == NULL){
				printf("client host information not found\n");
                close(conn_sd);
                exit(2);
			}
			else printf("Server (figlio): host client e' %s \n", host->h_name);

            //Leggo il numero della linea
            if((read(conn_sd,&numLine,sizeof(int))>0))
                printf("Server (figlio), linea da eliminare: %d\n",numLine);

            //inizializzo il numero della linea a 1 per indicare che
            //leggo la prima linea
            lcount=1;

            gettimeofday(&t1,NULL);

            //Sostituzione delle righe
            while((nread=read(conn_sd,&car,sizeof(char)))!=0){
                if(nread<0){
                    sprintf(error,"(PID %d) impossibile leggere dal file",getpid());
                    perror(error);
                    exit(EXIT_FAILURE);
                }
                //leggo e scrivo 1 byte alla volta, posso fare meglio?
                if(lcount!=numLine){
                    write(conn_sd,&car,1);
                }
                if(car=='\n'){
                    //printf("linea %d terminata\n",lcount);
                    lcount++;
                }/*
                if(car=='\n'){
                    lcount++;
                    write(conn_sd,&car,1);
                }else{
                    if(lcount==numLine)
                        printf("linea da eliminare\n");
                    else
                        write(conn_sd,&car,1);
                }*/
            }

            gettimeofday(&t2,NULL);
            trascorso= (t2.tv_sec-t1.tv_sec)*1000.0;
            printf("---Tempo impiegato : %f ms---\n",trascorso);
            if(numLine>lcount)
                printf("Numero linea da eliminare maggiore delle righe nel file\n");
            //liberole risorse non più utilizzate
			close(conn_sd);
			exit(0);
		} // figlio
		close(conn_sd);  // padre chiude socket di connessione non di scolto
	} // ciclo for infinito
}

