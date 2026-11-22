grammar Smoke;

// Tiny test grammar: one or more statements like "ping door;"
program   : statement+ EOF ;
statement : 'ping' ID ';' ;

ID : [a-zA-Z_] [a-zA-Z_0-9]* ;
WS : [ \t\r\n]+ -> skip ;