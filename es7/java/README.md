# *How the files of Server/Client/RegistryRemoto were divided into the folders?*
---
## [<ins>RegistryRemoto</ins>](https://github.com/cronoimpius/reti_di_calcolatori/tree/main/es7/java/RegistryRemoto)
1. Import **ALL** RegistryRemoto files ( also the stub and skeleton ), including also Interfaces files;
2. Import the Server .class files, including also **ALL** the .class of Interfaces  
used by the Server files and Stub;
4. Import other classes used by methods and implemented by the programmer  
( for example Programma.class used by the ServerCongresso ).

## [<ins>Server</ins>](https://github.com/cronoimpius/reti_di_calcolatori/tree/main/es7/java/Server)
1. Import the Stub created in RegistryRemoto folder;
2. Import **ALL** the .class file of the interfaces used by RegistryRemoto;
3. Import the .java of Server (Servers) and interfaces used by them.  
After we will compile this .java for obtain .class files ( also Stub and Skeleton );
5. Import other classes used by methods and implemented by the programmer  
( for example Programma.class used by the ServerCongresso ).

## [<ins>Client</ins>](https://github.com/cronoimpius/reti_di_calcolatori/tree/main/es7/java/Client)
1. Import the Stub created in RegistryRemoto folder;
2. Import **ALL** the .class file of the interfaces used by RegistryRemoto;
3. Import **ALL** the .class files of Servers used and interfaces related with them includeing the Servers stub;
4. Import the .java of Client (Clients) and interfaces used by them.  
After we will compile this .java for obtain .class files;
6. Import other classes used by methods and implemented by the programmer  
( for example Programma.class used by the ServerCongresso ).
---
# *Important thoughts*
1. It's important to compile the files in the folder and then copy/paste in the other folder.  
This is why is important to follow the order given : **RegistryRemoto->Server->Client**
2. In **ALL** the folder must be present the *rmi.policy* file.
3. For execute correctly the files in different folders follow the following steps [^1] :
    - Execute the rmiregistry in RegistryRemoto folder;
    - Start the RegistryRemoto in its folder;
    - Start the Server/Servers in its/their folder;
    - Execute the Client/Clients in its/their folder.
  
[^1]: When we execute all this programs from terminal we use java -Djava.security.policy=rmi.policy  
for make the security manager recognise the security file.
