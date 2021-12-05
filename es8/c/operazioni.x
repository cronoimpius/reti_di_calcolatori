const MAXLENGTH = 256;

struct infoFile {int nChar; int nWord; int nLine;};

struct req_dir {string nomeDir<MAXLENGTH>; int limit;};


program OPFILEPROGRAM{
    version OPFILEVERS{
        infoFile file_scan(string) = 1;
        int dir_scan(req_dir) = 2;
    } = 1;
} = 0x20000001;

