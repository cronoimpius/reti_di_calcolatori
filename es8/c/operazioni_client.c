/* operazioni_client.c
 *
 */

#include <stdio.h>
#include <string.h>
#include <rpc/rpc.h>
#include <time.h>
#include "operazioni.h"

int main(int argc, char *argv[]){

	CLIENT *cl;
	infoFile *ris;
	char *server;
	char s, dir[MAXLENGTH], *fileName, input[MAXLENGTH];
	int *res;
	req_dir *req;
        //definizione variabili per testing
    	clock_t start, end;

	if (argc != 2) 
	{
		fprintf(stderr, "uso: %s host \n", argv[0]);
		exit(1);
	}

	server = argv[1];
	cl = clnt_create(server, OPFILEPROGRAM, OPFILEVERS, "tcp");
	if (cl == NULL) 
	{
		clnt_pcreateerror(server);
		exit(1);
	}
	printf("Creato lo stub client con successo.\n");


	printf("Quale servizio vuoi utilizzare? (F= file scan, D= dir scan)\n");

	while((s=getc(stdin))!= EOF)
	{
		while(getc(stdin) != '\n');//per pulire il buffer

		if (s != 'F' && s != 'D')
		{
			printf("Il tipo di operazione deve essere 'F' o 'D'\n");
			printf("Quale servizio vuoi utilizzare? (ctrl+D per terminare)\n");
			continue;
		}

		if(s=='F')
		{
			printf("Inserisci il nome di un file:\n");

			if(gets(input)==EOF)
				break;
			
			fileName=input;
			start=clock();
			ris = file_scan_1(&fileName, cl);
			end=clock();

			if (ris == NULL) 
			{
				clnt_perror(cl, server);
				printf("Errore nell'analisi del file.\n\n");
				printf("Quale servizio vuoi utilizzare? (F= file scan, D= dir scan)\n");
				continue;
			}
			
			if(ris->nChar == -1)
				printf("File not found.\n");
			else{
				printf("Risultato ricevuto da %s: caratteri %d , parole %d, righe %d\n", server, ris->nChar, ris->nWord, ris->nLine);
				printf("---Tempo di esecuzione %f ---\n",(float)(end-start)/CLOCKS_PER_SEC);
			}

		}//case:F
		else
		{

			printf("Inserisci il nome di un direttorio:\n");
			if(gets(input)==EOF)
				break;

			req=malloc(strlen(input)+sizeof(int));

			//salvo il nome della directory in un array secondario
			strcpy(dir, input);
			req->nomeDir=dir;

			printf("Scegli una soglia minima: \n");
			
			if(gets(input)==EOF)
				break;

			for(int i = 0; input[i] != '\0'; i++){
				if(input[i] < '0' || input[i] > '9'){
					printf("Integer required.\n");
					printf("Quale servizio vuoi utilizzare? (ctrl+D per terminare)\n");
					continue;
				}
			}
			req->limit=atoi(input);

			if(req->limit<0)
			{
				printf("Intero negativo.\n");
				printf("Quale servizio vuoi utilizzare? (ctrl+D per terminare)\n");
				continue;
			}
			start=clock();
			res = dir_scan_1(req, cl);
			end=clock();

			if (res == NULL) 
			{
				clnt_perror(cl, server);
				printf("Quale servizio vuoi utilizzare? (ctrl+D per terminare)\n");
				continue;
			}
			if(*res == -1)
				printf("Apertura del direttorio fallita.\n");
			else{
				printf("File che superano %d in %s: %d\n", req->limit, req->nomeDir, *res);
				printf("---Tempo di esecuzione %f ---\n",(float)(end-start)/CLOCKS_PER_SEC);
			}

			free(req);
		}//case:D
		printf("Quale servizio vuoi utilizzare? (ctrl+D per terminare)\n");
	}//while
	clnt_destroy(cl);
	return 0;
}
