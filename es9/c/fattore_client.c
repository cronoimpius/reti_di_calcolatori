/*
  fattore_client.c
*/
#include "fattore.h"
#include <fcntl.h>
#include <rpc/clnt.h>
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>

int main(int argc, char *argv[]) {
  char *server, op[2];
  int i, ris;
  CLIENT *cl;
  Input input;
  Output *output;

  // controllo argomenti
  if (argc != 2) {
    fprintf(stderr, "Client usage: %s host\n", argv[0]), exit(1);
  }
  server = argv[1];

  // connessione
  cl = clnt_create(server, FATTORE, FATTOREVERS, "udp");
  if (cl == NULL) {
    clnt_pcreateerror(server);
    exit(1);
  }

  input.name = malloc(255 * sizeof(char));
  input.operation = malloc(12 * sizeof(char));

  printf("Premi 1 per stampa classifica, 2 per aggiungere un candidato, 3 per "
         "sottrarre un voto, EOF per terminare ");
  while (gets(op)) {
    if (op[0] == '1') {
      output = gudges_rank_1(&input, cl);
      i = 0;
      while (i < NGUDGES) {
        printf("%d - %s \n", i + 1, output->giudici[i]);
        i++;
      }
    } else if (op[0] == '2') {
      printf("Inserire nome candidato: ");
      gets(input.name);
      strcpy(input.operation, "aggiunta");
      ris = *vote_1(&input, cl);
      if (ris < 0)
        printf("Errore nell'espressione del voto\n");
      else
        printf("I voti attuali di %s sono %d\n", input.name, ris);
    } else if (op[0] == '3') {
      printf("Inserire il nome del canddidato: ");
      gets(input.name);
      strcpy(input.operation, "sottrazione");
      ris = *vote_1(&input, cl);
      if (ris < 0)
        printf("Errore nell'espreimere il voto\n");
      else
        printf("I voti attuali di %s sono: %d", input.name, ris);
    }
    printf("Premi 1 per Stampa classifica giudici, 2 per aggiungere un voto a "
           "un candidato, 3 per sottrarre un voto a un candidato o EOF per "
           "terminare: ");
  }
  clnt_destroy(cl);
  free(input.name);
  free(input.operation);
  exit(0);
}
