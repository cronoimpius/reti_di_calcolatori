/* Server Select
 * 	Un solo figlio per tutti i file.
 */

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
#include <time.h>

#define DIM_BUFF 100
#define LENGTH_FILE_NAME 20

#define max(a,b) ((a) > (b) ? (a) : (b))


 /********************************************************/
typedef struct {
	char fileName[30];
	char parola[30];
 }FParola;

/********************************************************/
void gestore(int signo){
	int stato;
	printf("esecuzione gestore di SIGCHLD\n");
	wait(&stato);
}
/********************************************************/

int main(int argc, char **argv){
    clock_t startDatagram, endDatagram, startStream, endStream;
	int  tcpfd, connfd, udpfd, fd_file, nready, maxfdp1;
	const int on = 1;
	char ch[2], nome_file[LENGTH_FILE_NAME], nome_dir[LENGTH_FILE_NAME], word[LENGTH_FILE_NAME];
	fd_set rset;
	int len, nread,  port, count=0;
	struct sockaddr_in cliaddr, servaddr;
    FParola fparola;

	/* CONTROLLO ARGOMENTI ---------------------------------- */
	if(argc!=2){
		printf("Error: %s port\n", argv[0]);
		exit(1);
	}
	nread = 0;
	while (argv[1][nread] != '\0'){
		if ((argv[1][nread] < '0') || (argv[1][nread] > '9')){
			printf("Terzo argomento non intero\n");
			exit(2);
		}
		nread++;
	}
	port = atoi(argv[1]);
	if (port < 1024 || port > 65535){
		printf("Porta scorretta...");
		exit(2);
	}

	/* INIZIALIZZAZIONE INDIRIZZO SERVER ----------------------------------------- */
	memset ((char *)&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = INADDR_ANY;
	servaddr.sin_port = htons(port);

	printf("Server avviato\n");

	/* CREAZIONE SOCKET TCP ------------------------------------------------------ */
	tcpfd=socket(AF_INET, SOCK_STREAM, 0);
	if (tcpfd <0)
	{perror("apertura socket TCP "); exit(1);}
	printf("Creata la socket TCP d'ascolto, fd=%d\n", tcpfd);

	if (setsockopt(tcpfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on))<0)
	{perror("set opzioni socket TCP"); exit(2);}
	printf("Set opzioni socket TCP ok\n");

	if (bind(tcpfd,(struct sockaddr *) &servaddr, sizeof(servaddr))<0)
	{perror("bind socket TCP"); exit(3);}
	printf("Bind socket TCP ok\n");

	if (listen(tcpfd, 5)<0)
	{perror("listen"); exit(4);}
	printf("Listen ok\n");

	/* CREAZIONE SOCKET UDP ------------------------------------------------ */
	udpfd=socket(AF_INET, SOCK_DGRAM, 0);
	if(udpfd <0)
	{perror("apertura socket UDP"); exit(5);}
	printf("Creata la socket UDP, fd=%d\n", udpfd);

	if(setsockopt(udpfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on))<0)
	{perror("set opzioni socket UDP"); exit(6);}
	printf("Set opzioni socket UDP ok\n");

	if(bind(udpfd,(struct sockaddr *) &servaddr, sizeof(servaddr))<0)
	{perror("bind socket UDP"); exit(7);}
	printf("Bind socket UDP ok\n");

	/* AGGANCIO GESTORE PER EVITARE FIGLI ZOMBIE -------------------------------- */
	signal(SIGCHLD, gestore);

	/* PULIZIA E SETTAGGIO MASCHERA DEI FILE DESCRIPTOR ------------------------- */
	FD_ZERO(&rset);
	maxfdp1=max(tcpfd, udpfd)+1;

	/* CICLO DI RICEZIONE EVENTI DALLA SELECT ----------------------------------- */
	for(;;){
		FD_SET(tcpfd, &rset);
		FD_SET(udpfd, &rset);

		if ((nready=select(maxfdp1, &rset, NULL, NULL, NULL))<0){
			if (errno==EINTR) continue;
			else {perror("select"); exit(8);}
		}

		/* GESTIONE RICHIESTE DI NOME DI UN FILE DA DIRETTORIO DI SECONDO LIVELLO ------------------------------------- */
		if (FD_ISSET(tcpfd, &rset)){
			printf("Ricevuta richiesta di get di un file\n");
			len = sizeof(struct sockaddr_in);
			if((connfd = accept(tcpfd,(struct sockaddr *)&cliaddr,&len))<0){
				if (errno==EINTR) continue;
				else {perror("accept"); exit(9);}
			}

			if (fork()==0){ /* processo figlio che serve la richiesta di operazione */
				close(tcpfd);
				printf("Dentro il figlio, pid=%i\n", getpid());
				DIR *fd_dir, *fd_dir2, *fd_dir3;
				struct dirent *dd, *dd2;
				char path1[256], path2[256];

				//aspetto il nome di una directory
				while((read(connfd, nome_dir, sizeof(nome_dir))) > 0)
				{
					printf("Richiesto dir %s\n", nome_dir);

					//provo ad aprirla per vedere se esiste
					fd_dir= opendir(nome_dir);
					//se non esiste torno un messaggio di errore
					if (fd_dir==NULL){
						printf("Directory inesistente\n");
						write(connfd, "-1" , 1);
					}
					else{
						//ciclo per ogni elemento dentro alla directory
						while ((dd = readdir(fd_dir))!= NULL)
						{
							sprintf(path1, "%s/%s", nome_dir, dd->d_name);
							//controllo se l'elemento è una directory
							if ((fd_dir2 = opendir(path1))!=NULL && dd->d_name[0] != '.')
							{
								printf("apertura di %s\n", dd->d_name);
								//ciclo ogni elemento della sotto-directory
								while((dd2 = readdir(fd_dir2)) != NULL)
								{
									sprintf(path2, "%s/%s", path1, dd2->d_name);
									//controllo se l'elemento è un file
									if ((fd_dir3 = opendir(path2))==NULL && dd2->d_name[0]!= '.')
									{	//mando il nome del file al client
										printf("Trovato il file %s\n", dd2->d_name);
										write(connfd, dd2->d_name, strlen(dd2->d_name)+1);
										write(connfd, "\n", 1);
									}
									else{closedir(fd_dir3); continue;}
								}
								printf("chiusura %s\n", dd->d_name);
								closedir(fd_dir2);
							}
							else continue;
						}
					}//else
					printf("chiusura %s\n", nome_dir);
					closedir(fd_dir);
					//mando un carattere speciale al client per dirgli che i file sono terminati
					write(connfd, "*", 1);
					printf("In attesa di una directory...\n");
				}//while
				printf("Figlio %i: chiudo connessione e termino\n", getpid());
				close(connfd);
				exit(0);
			}//figlio

			/* padre chiude la socket dell'operazione */
			close(connfd);
		} /* fine gestione richieste di file */

		/* GESTIONE RICHIESTE DI ELIMINAZIONE PAROLA DATAGRAM------------------------------------------ */


		if (FD_ISSET(udpfd, &rset)){
            int fdfd;
            char buffer[DIM_BUFF]="";
			printf("Server: ricevuta richiesta dell'eliminazione della parola\n");
			len=sizeof(struct sockaddr_in);

            startDatagram=clock();
			if (recvfrom(udpfd, &fparola, sizeof(FParola), 0, (struct sockaddr *)&cliaddr, &len)<0)
			{perror("recvfrom"); continue;}


			strcpy(nome_file,fparola.fileName);
			strcpy(word,fparola.parola);

			printf("Richiesta eliminazione della parola %s nel file %s\n", word, nome_file);

			if ((fd_file = open(nome_file, O_RDONLY) )< 0) {
				printf("File inesistente\n");
				count = -1;
			}
			else {
                if((fdfd=open("temp", O_WRONLY|O_CREAT|O_TRUNC, 0644))<0)
                    perror("open");

				while ((read(fd_file, &ch, sizeof(char))) > 0) {

					if (strcmp(ch, " ")==0 || strcmp(ch,"\n")==0 || strcmp(ch, ",")==0 || strcmp(ch,">>")==0) {
						if (strcmp(word, buffer)==0){
							count=count+1;
                        }

                       else{
                            strcat(buffer, ch);
//                         printf("Parola : %s, Count: %d\n", buffer, count);
                        write(fdfd, &buffer, strlen(buffer)+1);
                    }
                        strcpy(buffer, "");
                    }else
					strcat(buffer, ch);
				}
			}
			endDatagram=clock();
			printf("Fine ricerca parola %s: numero di volte in cui c'è stato %d\n",word, count);

            printf("Tempo di escuzione byte per byte: %f\n", (float)(endDatagram-startDatagram)/CLOCKS_PER_SEC);
			if (sendto(udpfd, &count, sizeof(count), 0, (struct sockaddr *)&cliaddr, len)<0)
			{perror("sendto"); continue;}

            count=0;
			printf("Ho inviato il numero di parole eliminate\n");

		} /* fine gestione richieste di conteggio */
	} /* ciclo for della select */
}
