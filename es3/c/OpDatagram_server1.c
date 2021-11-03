/* Server che fornisce la valutazione di un'operazione tra due interi */
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <string.h>
#include <time.h>

#define LENGTH 256


int main(int argc, char **argv){
    
    clock_t start, end;
    
	int sd, port, len, num1, max;
	const int on = 1;
	struct sockaddr_in cliaddr, servaddr;
	struct hostent *clienthost;
	char nomeFile[LENGTH], parola[LENGTH];
    int fd,n, count=0;
    char ch;
	/* CONTROLLO ARGOMENTI ---------------------------------- */
	if(argc!=2){
		printf("Error: %s port\n", argv[0]);
		exit(1);
	}
	else{
		num1=0;
		while( argv[1][num1]!= '\0' ){
			if((argv[1][num1] < '0') || (argv[1][num1] > '9')){
				printf("Secondo argomento non intero\n");
				printf("Error: %s port\n", argv[0]);
				exit(2);
			}
			num1++;
		}  	
	  	port = atoi(argv[1]);
  		if (port < 1024 || port > 65535){
		      printf("Error: %s port\n", argv[0]);
		      printf("1024 <= port <= 65535\n");
		      exit(2);  	
  		}
	}

	/* INIZIALIZZAZIONE INDIRIZZO SERVER ---------------------------------- */
	memset ((char *)&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = INADDR_ANY;  
	servaddr.sin_port = htons(port);  

	/* CREAZIONE, SETAGGIO OPZIONI E CONNESSIONE SOCKET -------------------- */
	sd=socket(AF_INET, SOCK_DGRAM, 0);
	if(sd <0){perror("creazione socket "); exit(1);}
	printf("Server: creata la socket, sd=%d\n", sd);

	if(setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on))<0)
	{perror("set opzioni socket "); exit(1);}
	printf("Server: set opzioni socket ok\n");

	if(bind(sd,(struct sockaddr *) &servaddr, sizeof(servaddr))<0)
	{perror("bind socket "); exit(1);}
	printf("Server: bind socket ok\n");

	/* CICLO DI RICEZIONE RICHIESTE ------------------------------------------ */
	for(;;){
        max=0;
		len=sizeof(struct sockaddr_in);
		if (recvfrom(sd,nomeFile, sizeof(nomeFile), 0, (struct sockaddr *)&cliaddr, &len)<0)
		{perror("recvfrom "); continue;}
		
		
        
		if ((fd = open(nomeFile, O_RDONLY)) < 0) {
			perror("File inesistente");
			max=-1;
		}else{
        
        	start=clock();
			while ((n=read(fd, &ch ,sizeof(char)))>0) {
            
				if(ch==' ' || ch=='\n') {
                	if(count>max)
                    	max=count;
                	count=0;
            	}else
                	count++;
			}
			end=clock();
		}
		close(fd);
		clienthost=gethostbyaddr( (char *) &cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
		if (clienthost == NULL) printf("client host information not found\n");
		else printf("Operazione richiesta da: %s %i\n", clienthost->h_name,(unsigned)ntohs(cliaddr.sin_port)); 
        
        printf("Tempo di escuzione byte per byte: %f\n", (float)(end-start)/CLOCKS_PER_SEC);

		if (sendto(sd, &max, sizeof(max), 0, (struct sockaddr *)&cliaddr, len)<0)
		{perror("sendto "); continue;}
	} //for
	close(sd);
}
