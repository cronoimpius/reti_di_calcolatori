/*
  fattore.x 
  +definizione input e struttura (3xstrutture semplici) 
  +definizione dei metodi richiesti o restituiti
*/
const NGUDGES=4;

struct Input{
    string name<255>;
    string operation<12>;
};

struct Gudge{
    string name<255>;
};

struct Output{
    Gudge giudici[NGUDGES];
};

program FATTORE {
    version FATTOREVERS{
        int vote(Input)=1;
        Output gudges_rank(void)=2;
      }=1;
}=0x20000013;
