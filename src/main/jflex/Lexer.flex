package com.michi.analizadorlexico;

%%

%public
%class Lexer
%unicode
%line
%column
%type Token

%state STRING
%state COMMENT_BLOCK

%{
  /** Token con tipo, lexema, línea y columna (1-based para mostrar) */
  public static class Token {
    public final String type;
    public final String lexeme;
    public final int line;
    public final int col;

    public Token(String type, String lexeme, int line0, int col0) {
      this.type = type;
      this.lexeme = lexeme;
      this.line = line0 + 1;
      this.col  = col0  + 1;
    }

    @Override
    public String toString() {
      return type + "(" + lexeme + ")";
    }
  }

  private Token tok(String type) {
    return new Token(type, yytext(), yyline, yycolumn);
  }
  private Token tok(String type, String lex) {
    return new Token(type, lex, yyline, yycolumn);
  }

  private Token err(String message) {
    return new Token("ERROR", message, yyline, yycolumn);
  }

  private StringBuilder str = new StringBuilder();
  private int strLine0 = 0;
  private int strCol0  = 0;

  private Token stringTok() {
    return new Token("STRING", str.toString(), strLine0, strCol0);
  }
%}

/* ==sdfasdf== */
Whitespace    = [ \t\f]+
NewLine       = \r\n|\r|\n

Digit         = [0-9]
Int           = {Digit}+
Dec           = {Int}"."{Int}     /* 123.45 */
Exp           = ([eE][+-]?{Int})
Num           = ({Dec}|{Int})({Exp})?

IdStart       = [A-Za-z_]
IdPart        = [A-Za-z0-9_]
Identifier    = {IdStart}{IdPart}*

%%

/* ======== NORMAL ======== */
<YYINITIAL>{

  {Whitespace}               { /* ignore */ }
  {NewLine}                  { /* ignore */ }

  /* Comentarios: primero los comentarios, luego el / como operador */
  "//".*                     { /* ignore */ }
  "/*"                       { yybegin(COMMENT_BLOCK); }

  /* Strings */
  "\""                       { str.setLength(0); strLine0 = yyline; strCol0 = yycolumn; yybegin(STRING); }

  /* Keywords */
  "if"|"else"|"while"|"for"|"return"|"int"|"double"|"string"|"true"|"false"
                             { return tok("KEYWORD"); }

  /* Operadores compuestos (maximal munch) */
  "<="                       { return tok("LESS_EQUAL"); }
  ">="                       { return tok("GREATER_EQUAL"); }
  "=="                       { return tok("EQUAL_EQUAL"); }
  "!="                       { return tok("NOT_EQUAL"); }
  "&&"                       { return tok("AND"); }
  "||"                       { return tok("OR"); }

  /* Operadores simples */
  "+"                        { return tok("PLUS"); }
  "-"                        { return tok("MINUS"); }
  "*"                        { return tok("MULTIPLY"); }
  "/"                        { return tok("DIVIDE"); }
  "="                        { return tok("ASSIGN"); }
  "<"                        { return tok("LESS"); }
  ">"                        { return tok("GREATER"); }

  /* Agrupación */
  "("                        { return tok("LPAREN"); }
  ")"                        { return tok("RPAREN"); }
  "{"                        { return tok("LBRACE"); }
  "}"                        { return tok("RBRACE"); }
  "["                        { return tok("LBRACKET"); }
  "]"                        { return tok("RBRACKET"); }

  /* Delimitadores */
  ";"                        { return tok("SEMICOLON"); }
  ","                        { return tok("COMMA"); }
  "."                        { return tok("DOT"); }

  /* Números */
  {Num}                      { return tok("NUMBER"); }

  /* Números mal formados (ej: 12. o .5 o 1e o 1e+) */
  {Int}"."                   { return err("NUMERO_MAL_FORMADO:" + yytext()); }
  "."{Int}                   { return err("NUMERO_MAL_FORMADO:" + yytext()); }
  {Int}([eE][+-]?)           { return err("NUMERO_MAL_FORMADO:" + yytext()); }

  /* Identificadores */
  {Identifier}               { return tok("IDENTIFIER"); }

  /* Identificador mal formado (12abc) */
  {Int}{Identifier}          { return err("IDENTIFICADOR_MAL_FORMADO:" + yytext()); }

  /* Símbolo inválido */
  .                          { return err("SIMBOLO_INVALIDO:" + yytext()); }
}

/* ======== STRING ======== */
<STRING>{
  "\""                       { yybegin(YYINITIAL); return stringTok(); }

  "\\n"                      { str.append('\n'); }
  "\\t"                      { str.append('\t'); }
  "\\r"                      { str.append('\r'); }
  "\\\""                     { str.append('\"'); }
  "\\\\"                     { str.append('\\'); }

  /* escape genérico: \x -> guarda x */
  "\\."                      { str.append(yytext().charAt(1)); }

  /* contenido normal (sin backslash, sin comillas, sin salto de línea) */
  [^\\\"\r\n]+               { str.append(yytext()); }

  /* salto de línea dentro del string => no cerrada */
  {NewLine}                  { yybegin(YYINITIAL); return new Token("ERROR", "CADENA_NO_CERRADA", strLine0, strCol0); }

  .                          { str.append(yytext()); }
}

/* ======== COMMENT_BLOCK ======== */
<COMMENT_BLOCK>{
  "*/"                       { yybegin(YYINITIAL); }

  /* consume cualquier cosa dentro del comentario, incluyendo nuevas líneas */
  (.|{NewLine})              { /* ignore */ }

  <<EOF>>                    { yybegin(YYINITIAL); return err("COMENTARIO_NO_CERRADO"); }
}

/* EOF normal */
<<EOF>>                      { return null; }