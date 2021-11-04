/* Server Select
 * 	la richiesta di eliminare le occorrenze di una parola viene gestita
 * 	in modo sequenziale usando socket datagram.
 *
 * 	le richieste per ottenere i nomi dei file nei sottodirectory sono
 * 	gestite concorrentemente usando socket stream (unica connessione
 * 	per gestire la sessione col client).
 *
 *
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

#define DIM_BUFF 100
#define LENGTH_FILE_NAME 30
#define max(a,b)((a)>(b)?(a):(b))

/************************************************/

typedef struct
{
    char fileN[LENGTH_FILE_NAME];
    char word[LENGTH_FILE_NAME];
}Request;

void handler(int signo){
	int state;
	printf("esecuzione gestore di SIGCHLD\n");
	wait(&state);
}
/********************************************************/

int main(int argc, char **argv){
	int  listenfd, connfd, udpfd, fd_file, nready, maxfdp1;
	const int on = 1;
	char zero=0, buff[DIM_BUFF], nfile[LENGTH_FILE_NAME], ndir[LENGTH_FILE_NAME];
    char dir[DIM_BUFF], fileNout[LENGTH_FILE_NAME];
    DIR *dir1, *dir2, *dir3;
    struct dirent *dd1,*dd2;
	fd_set rset;
    Request dat;
	int len, nread, nwrite, num, ris, port;
	struct sockaddr_in cliaddr, servaddr;
    FILE *fd_outudp, *fd_inudp;
    struct hostent *hostTCP, *hostUDP;

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
	listenfd=socket(AF_INET, SOCK_STREAM, 0);
	if (listenfd <0)
	{perror("apertura socket TCP "); exit(1);}
	printf("Creata la socket TCP d'ascolto, fd=%d\n", listenfd);

	if (setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on))<0)
	{perror("set opzioni socket TCP"); exit(2);}
	printf("Set opzioni socket TCP ok\n");

	if (bind(listenfd,(struct sockaddr *) &servaddr, sizeof(servaddr))<0)
	{perror("bind socket TCP"); exit(3);}
	printf("Bind socket TCP ok\n");

	if (listen(listenfd, 5)<0)
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
	signal(SIGCHLD, handler);

	/* PULIZIA E SETTAGGIO MASCHERA DEI FILE DESCRIPTOR ------------------------- */
	FD_ZERO(&rset);
	maxfdp1=max(listenfd, udpfd)+1;

	/* CICLO DI RICEZIONE EVENTI DALLA SELECT ----------------------------------- */
	for(;;){
		FD_SET(listenfd, &rset);
		FD_SET(udpfd, &rset);

		if ((nready=select(maxfdp1, &rset, NULL, NULL, NULL))<0){
			if (errno==EINTR) continue;
			else {perror("select"); exit(8);}
		}

		/* GESTIONE RICHIESTE UDP ------------------------------------- */
		if (FD_ISSET(udpfd, &rset)){
			printf("Ricevuta richiesta UDP: eliminazione di occorrenze di una parola\n");
			len = sizeof(struct sockaddr_in);

			if(recvfrom (udpfd,&dat,sizeof(dat),0,(struct sockaddr *)&cliaddr, &len)<0){
				perror("recvfrom"); continue;
			}

            printf("Richiesta modifica file: %s per la parola: %s\n",dat.fileN,dat.word);
            hostUDP=gethostbyaddr((char *)&cliaddr.sin_addr,sizeof(cliaddr.sin_addr),AF_INET);
            if(hostUDP==NULL) printf("client host information not found\n");
            else printf("Operazione richiesta da: %s %i\n",hostUDP->h_name,(unsigned)ntohs(cliaddr.sin_port));

            //verifico l'esistenza del file
            int ris=0;
            fd_inudp=fopen(dat.fileN,"rt");
            fileNout[0]='\0';

            strcat(fileNout,dat.fileN);
            strcat(fileNout,"_out.txt");

            fd_outudp=fopen(fileNout,"wt");
            if(!fd_outudp || !fd_inudp){
                perror("Errore apertura file");
                ris=-1;
            }else{
                char buf[128],ch;
                while(fscanf(fd_inudp,"%s",buf)!=EOF){
                    if(strcmp(buf,dat.word)){
                    //entro qui se le parole hanno dimensione diversa
                    //dunque la parola non è quella da eliminare

                        fwrite(buf, strlen(buf),1,fd_outudp);
                    }
                    else
                        ris++; //aumento il contatore delle occorrenze quando
                            //non mando la linea: la elimino
                }
                    fputc('\n',fd_outudp);
            }



            printf("Nel file %s sono state eliminate %d le occorrenze di %s\n",dat.fileN,ris,dat.word);

            if(sendto(udpfd, &ris, sizeof(int),0,(struct sockaddr *)&cliaddr,len)<0){
                perror("sendto");
                continue;
            }

            printf("Server: riavvio...\n");
            fclose(fd_inudp);
            fclose(fd_outudp);
        }

            /* GESTIONE RICHIESTE TCP----------------------------------- */
            if(FD_ISSET(listenfd,&rset)){
                printf("Ricevuta richiesta TCP: cerco file delle cartelle \n");
                len=sizeof(cliaddr);
                if((connfd=accept(listenfd,(struct sockaddr *)&cliaddr,&len))<0){
                    if(errno==EINTR){
                        perror("continua l'accept");
                        continue;
                    }else
                        exit(9);
                }

			if (fork()==0){ /* processo figlio che serve la richiesta di operazione */
			    close(listenfd);
                hostTCP=gethostbyaddr((char *)&cliaddr.sin_addr,sizeof(cliaddr.sin_addr),AF_INET);
                if(hostTCP==NULL){
                    printf("client host information not found\n");
                    close(connfd);
                    exit(10);
                }
                else
                    printf("Server (figlio) host client è %s\n",hostTCP->h_name);

                //Leggo richiesta del client
                while((nread=read(connfd,dir,sizeof(dir)))>0){
                    printf("Server (figlio) nella cartella: %s\n",dir);

                    char r;//usato per separare sulla connessione unica
                    if((dir1=opendir(dir))!=NULL){
                        while ((dd1=readdir(dir1))!=NULL){
                            if(strcmp(dd1->d_name,".")!=0 && strcmp(dd1->d_name,"..")!=0){
                                ndir[0]='\0';
                                strcat(ndir,dir);
                                strcat(ndir,"/");
                                strcat(ndir,dd1->d_name);
                                if((dir2=opendir(ndir))!=NULL ){ //cartella secondo livello
                                    while((dd2=readdir(dir2))!=NULL){
                                        if(strcmp(dd2->d_name,".")!=0 && strcmp(dd2->d_name,"..")!=0){
                                            strcat(ndir,"/");
                                            strcat(ndir,dd2->d_name);
                                            if((dir3 = opendir(ndir))==NULL && strcmp(dd2->d_name, ".") && strcmp(dd2->d_name,"..") ){
                                                //file nella cartella di secondo livello
                                                printf("%s è un file che ci interessa\n",dd2->d_name);
                                                strcpy(nfile, dd2->d_name);
                                                strcat(nfile,"\0");
                                                if(write(connfd,nfile,(strlen(nfile)+1))<0){
                                                    perror("Errore invio il file");
                                                    continue;
                                                }
                                            }// if sui file
                                            else{ closedir(dir3);continue;}
                                        }// escludo il . e il .. secondo livello
                                    }// fine while cartella di secondo livello
                                    printf("fine invio\n");
                                }// se trovo una cartella di secondo livello
                                else{ closedir(dir2); continue;}
                            }// escludo il . e il .. nel primo livello
                        }// fine while cartella di primo livello
                    }// se trovo una cartella
                    else {
                        printf("Errore nell'apertura di %s\n",dir);
                        write(connfd,"-1",sizeof(char));
                    }
                    closedir(dir1);
                    r='*';
                    write(connfd,&r,sizeof(char));
                    printf("altra dir\n");
                }//fine while read
            //Free risorse
			printf("Figlio TCP %i: chiudo connessione e termino\n", getpid());
			close(connfd);
			exit(0);
		}//figlio
		/* padre chiude la socket dell'operazione */
	    close(connfd);
	    }/*if TCP  */
    }//for
}//main
