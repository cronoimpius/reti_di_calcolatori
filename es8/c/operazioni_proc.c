#include <stdio.h>
#include <stdlib.h>
#include <rpc/rpc.h>
#include <sys/dir.h>
#include <fcntl.h>
#include <unistd.h>
#include <time.h>
#include "operazioni.h"

infoFile *file_scan_1_svc(char **file, struct svc_req *rp){
	static infoFile ris;
    char ch;
	int n, fd, first = 0;
	//definizione variabili per testing
    	clock_t start, end;

    if(*file == NULL) exit(1);
	printf("File ricevuto: %s\n", *file);
    if ((fd = open(*file, O_RDONLY)) < 0)
    {
		perror("File inesistente");
        ris.nChar=-1; ris.nLine=-1; ris.nWord=-1;
    }
    else
	{
	    start=clock();
	    while ((n=read(fd, &ch ,1))>0) 
		{
            if(ch!=' ' && ch!='\n')
            {
                if(first==0) first = 1;
                ris.nChar++;
            }

            if(ch==' ' || ch==';' || ch=='.' || ch==':' || ch==',' || (ch=='\n' && first == 1))
                ris.nWord++;

            if(ch=='\n')
            {
                ris.nLine++;
                first=0;
            }

        }
    }
	end=clock();
	printf("---Tempo di esecuzione %f ---\n",(float)(end-start)/CLOCKS_PER_SEC);
	close(n);

    ris.nLine++;
	return (&ris);
}

int *dir_scan_1_svc(req_dir *req, struct svc_req *rp)
{
    static int result;
    DIR *dir;
    int f;
    struct dirent *d;
    char pathD[MAXLENGTH] = "", pathF[MAXLENGTH];//una variabile per il path della directory e una per i file

    //definizione variabili per testing
    clock_t start, end;

    printf("Parametri richiesti: directory:%s  dimMax:%d\n", req->nomeDir, req->limit);

    if((dir=opendir(req->nomeDir)) == NULL)
    {
        printf("Apertura di %s fallita.\n", req->nomeDir);
        result=-1;
    }
    else
    {
	start=clock();
        int count=0;
        strcat(pathD, req->nomeDir); strcat(pathD, "/");

        while(d=readdir(dir))
        {
            strcpy(pathF, pathD); strcat(pathF, d->d_name);
            if((f=open(pathF, O_RDONLY))>0 && d->d_name[0]!='.')
            {
                printf("aperto il file:%s\n", d->d_name);
                if((int)lseek(f, 0, 2) > req->limit)
                    count++;
                close(f);
            } 
        }
        result=count;
    }
    end=clock();
    printf("---Tempo di esecuzione %f ---\n",(float)(end-start)/CLOCKS_PER_SEC);
    closedir(dir);
    return &result;

}