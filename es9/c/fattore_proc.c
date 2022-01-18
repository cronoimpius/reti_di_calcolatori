/*
  fattore_proc.c
  +implementa le procedure remote
  +inizializza strutture
*/

#include <fcntl.h>
#include <rpc/rpc.h>
#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>

#include "fattore.h" //generato con rpcgen

/*Stato Server*/
#define TAB_DIM 20

typedef struct {
  char nome[256];
  char giudice[256];
  char categoria;
  char nomefile[256];
  char fase;
  int voti;
} dati_candidato;

dati_candidato candidate_table[TAB_DIM];

typedef struct {
  char nome[256];
  int voti_tot;
} dati_giudice;

dati_giudice gudge_table[4];
static int inizialized = 0;

void inizializza() {
  int i, j;

  if (inizialized == 1)
    return;
  inizialized = 1;

  // inizializzazione struttura

  for (i = 0; i < TAB_DIM; i++) {
    candidate_table[i].voti = 0;
    candidate_table[i].fase = 'L'; // per indicare che è libero
    candidate_table[i].categoria = 'L';
    strcpy(candidate_table[i].nome, "L");
    strcpy(candidate_table[i].giudice, "L");
    strcpy(candidate_table[i].nomefile, "L");
  }
  candidate_table[1].fase = 'B';
  candidate_table[1].categoria = 'U';
  strcpy(candidate_table[1].nome, "a");
  strcpy(candidate_table[1].giudice, "g1");
  strcpy(candidate_table[1].nomefile, "astjyx");
  candidate_table[2].fase = 'B';
  candidate_table[2].categoria = 'U';
  strcpy(candidate_table[2].nome, "b");
  strcpy(candidate_table[2].giudice, "g2");
  strcpy(candidate_table[2].nomefile, "artjyx");
  candidate_table[3].fase = 'B';
  candidate_table[3].categoria = 'D';
  strcpy(candidate_table[3].nome, "c");
  strcpy(candidate_table[3].giudice, "g2");
  strcpy(candidate_table[3].nomefile, "asgjyx");
  candidate_table[4].fase = 'D';
  candidate_table[4].categoria = 'D';
  strcpy(candidate_table[4].nome, "d");
  strcpy(candidate_table[4].giudice, "g1");
  strcpy(candidate_table[4].nomefile, "astjtx");
  candidate_table[5].fase = 'B';
  candidate_table[5].categoria = 'U';
  strcpy(candidate_table[5].nome, "e");
  strcpy(candidate_table[5].giudice, "g3");
  strcpy(candidate_table[5].nomefile, "astjyv");
  candidate_table[6].fase = 'B';
  candidate_table[6].categoria = 'U';
  strcpy(candidate_table[6].nome, "f");
  strcpy(candidate_table[6].giudice, "g2");
  strcpy(candidate_table[6].nomefile, "astjyx");
  candidate_table[7].fase = 'B';
  candidate_table[7].categoria = 'D';
  strcpy(candidate_table[7].nome, "g");
  strcpy(candidate_table[7].giudice, "g4");
  strcpy(candidate_table[7].nomefile, "astlyx");

  for (i = 0; i < 4; i++) {
    gudge_table[i].voti_tot = 0;
    strcpy(gudge_table[i].nome, "L");
  }
  strcpy(gudge_table[0].nome, "g1");
  strcpy(gudge_table[1].nome, "g2");
  strcpy(gudge_table[2].nome, "g3");
  strcpy(gudge_table[3].nome, "g4");
}

int ricerca(char *nome) {
  int i;
  for (i = 0; i < TAB_DIM; i++) {
    if (strcmp(nome, candidate_table[i].nome) == 0)
      return i;
  }
  return -1;
}

int totGudge(char *gudge) {
  int i, tot = 0;
  for (i = 0; i < TAB_DIM; i++) {
    if (strcmp(candidate_table[i].giudice, gudge) == 0)
      tot += candidate_table[i].voti;
  }
  return tot;
}

int *vote_1_svc(Input *input, struct svc_req *rqstp) {
  inizializza();
  // cerco il nome passato in input, se non presente restituisco -1

  int i = ricerca(input->name);
  if (i == -1) {
    printf("Nome non presente nella lista dei candidati.\n");
    static int err = -1;
    return &err;
  }

  if (strcmp(input->operation, "aggiunta") == 0) {
    candidate_table[i].voti++;
  } else if (strcmp(input->operation, "sottrazione") == 0) {
    if (candidate_table[i].voti == 0)
      printf("Operazione di sottrazione voto impossibile.\n");
    else
      candidate_table[i].voti--;
  } else {
    printf("Errore nel votare: operazione non valida\n");
  }
  return (&candidate_table[i].voti);
}

Output *gudges_rank_1_svc(void *in, struct svc_req *reqstp) {
  inizializza();
  static Output res;
  int i, j;
  int max = 0;
  dati_giudice temp;
  for (i = 0; i < NGUDGES; i++)
    gudge_table[i].voti_tot = totGudge(gudge_table[i].nome);

  for (i = 0; i < NGUDGES; i++) { // ordino i giudici
    for (j = 0; j < NGUDGES; j++) {
      if (gudge_table[j].voti_tot > gudge_table[max].voti_tot)
        max = j;
    }
    res.giudici[i].name = gudge_table[max].nome;
    gudge_table[max].voti_tot = -1;
  }
  return &res;
}
