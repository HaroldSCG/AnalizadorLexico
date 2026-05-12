package com.michi.analizadorlexico;

%%

%public
%class Lexer
%unicode
%line
%column
%type Token

%xstate STRING
%xstate COMMENT_BLOCK

%{
  /** Token con tipo, lexema, linea y columna (1-based para mostrar). */
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
      return String.format("[%d:%d] %s(%s)", line, col, type, lexeme);
    }
  }

  private Token tok(String type) {
    return new Token(type, yytext(), yyline, yycolumn);
  }

  private Token err(String type) {
    return new Token(type, yytext(), yyline, yycolumn);
  }

  /* Acumulador de literales de cadena. */
  private final StringBuilder str = new StringBuilder();
  private int strLine0 = 0;
  private int strCol0  = 0;

  private Token stringTok() {
    return new Token("STRING", str.toString(), strLine0, strCol0);
  }

  /* Estado de comentarios de bloque con anidamiento. */
  private int commentDepth = 0;
  private int commentLine0 = 0;
  private int commentCol0  = 0;
%}

/* === Definiciones === */
Whitespace    = [ \t\f]+
NewLine       = \r\n|\r|\n

Digit         = [0-9]
Int           = {Digit}+
Dec           = {Int}"."{Int}
Exp           = ([eE][+-]?{Int})
Num           = ({Dec}|{Int})({Exp})?

Hex           = 0[xX][0-9A-Fa-f]+
Bin           = 0[bB][01]+

IdStart       = [A-Za-z_]
IdPart        = [A-Za-z0-9_]
Identifier    = {IdStart}{IdPart}*

%%

/* === Estado NORMAL === */
<YYINITIAL> {

  {Whitespace}                       { /* ignorar */ }
  {NewLine}                          { /* ignorar */ }

  /* Comentarios (antes que el operador '/') */
  "//" [^\r\n]*                      { /* comentario de linea */ }
  "/*"                               { commentDepth = 1;
                                       commentLine0 = yyline;
                                       commentCol0  = yycolumn;
                                       yybegin(COMMENT_BLOCK); }

  /* Apertura de cadena */
  "\""                               { str.setLength(0);
                                       strLine0 = yyline;
                                       strCol0  = yycolumn;
                                       yybegin(STRING); }

  /* Literal de caracter */
  "'" ( [^\\'\r\n] | "\\" . ) "'"    { return tok("CHAR"); }
  "'" ( [^\\'\r\n] | "\\" . )        { return err("ERROR_CHAR_NO_CERRADO"); }

  /* Palabras reservadas */
  "if"|"else"|"while"|"for"|"return"|"int"|"double"|"string"|"true"|"false"
                                     { return tok("KEYWORD"); }

  /* Operadores compuestos (deben ir antes que los simples por maximal munch) */
  "<="                               { return tok("LESS_EQUAL"); }
  ">="                               { return tok("GREATER_EQUAL"); }
  "=="                               { return tok("EQUAL_EQUAL"); }
  "!="                               { return tok("NOT_EQUAL"); }
  "&&"                               { return tok("AND"); }
  "||"                               { return tok("OR"); }
  "++"                               { return tok("INC"); }
  "--"                               { return tok("DEC"); }
  "+="                               { return tok("PLUS_ASSIGN"); }
  "-="                               { return tok("MINUS_ASSIGN"); }
  "*="                               { return tok("MULT_ASSIGN"); }
  "/="                               { return tok("DIV_ASSIGN"); }

  /* Operadores simples */
  "+"                                { return tok("PLUS"); }
  "-"                                { return tok("MINUS"); }
  "*"                                { return tok("MULTIPLY"); }
  "/"                                { return tok("DIVIDE"); }
  "%"                                { return tok("MOD"); }
  "="                                { return tok("ASSIGN"); }
  "<"                                { return tok("LESS"); }
  ">"                                { return tok("GREATER"); }
  "!"                                { return tok("NOT"); }
  "?"                                { return tok("QUESTION"); }
  ":"                                { return tok("COLON"); }

  /* Agrupacion */
  "("                                { return tok("LPAREN"); }
  ")"                                { return tok("RPAREN"); }
  "{"                                { return tok("LBRACE"); }
  "}"                                { return tok("RBRACE"); }
  "["                                { return tok("LBRACKET"); }
  "]"                                { return tok("RBRACKET"); }

  /* Delimitadores */
  ";"                                { return tok("SEMICOLON"); }
  ","                                { return tok("COMMA"); }
  "."                                { return tok("DOT"); }

  /* Numeros: hex/bin antes que {Num} para desambiguar el prefijo '0' */
  {Hex}                              { return tok("NUMBER"); }
  {Bin}                              { return tok("NUMBER"); }
  {Num}                              { return tok("NUMBER"); }

  /* Errores numericos: solo ganan cuando {Num} NO logro un match mas largo. */
  {Int}"."                           { return err("ERROR_NUMERO_MALFORMADO"); }
  "."{Int}                           { return err("ERROR_NUMERO_MALFORMADO"); }
  {Int}([eE][+-]?)                   { return err("ERROR_NUMERO_MALFORMADO"); }

  /* Identificadores */
  {Identifier}                       { return tok("IDENTIFIER"); }

  /* Identificador mal formado (12abc, 0xGG, etc.) */
  {Int}{Identifier}                  { return err("ERROR_IDENTIFICADOR_MALFORMADO"); }

  /* Simbolo invalido */
  .                                  { return err("ERROR_SIMBOLO_INVALIDO"); }
}

/* === Estado STRING === */
<STRING> {
  "\""                               { yybegin(YYINITIAL); return stringTok(); }

  "\\n"                              { str.append('\n'); }
  "\\t"                              { str.append('\t'); }
  "\\r"                              { str.append('\r'); }
  "\\\""                             { str.append('\"'); }
  "\\\\"                             { str.append('\\'); }

  /* Escape Unicode \uHHHH */
  "\\u" [0-9A-Fa-f]{4}               { str.append((char) Integer.parseInt(yytext().substring(2), 16)); }

  /* Escape generico: \x -> x */
  "\\" .                             { str.append(yytext().charAt(1)); }

  /* Contenido normal (sin backslash, comilla ni salto de linea) */
  [^\\\"\r\n]+                       { str.append(yytext()); }

  /* Salto de linea sin cerrar => error */
  {NewLine}                          { yybegin(YYINITIAL);
                                       return new Token("ERROR_CADENA_NO_CERRADA",
                                                        str.toString(), strLine0, strCol0); }

  /* EOF dentro de cadena => error */
  <<EOF>>                            { yybegin(YYINITIAL);
                                       return new Token("ERROR_CADENA_NO_CERRADA",
                                                        str.toString(), strLine0, strCol0); }
}

/* === Estado COMMENT_BLOCK (con anidamiento) === */
<COMMENT_BLOCK> {
  "/*"                               { commentDepth++; }
  "*/"                               { if (--commentDepth == 0) yybegin(YYINITIAL); }

  (.|{NewLine})                      { /* ignorar contenido del comentario */ }

  <<EOF>>                            { yybegin(YYINITIAL);
                                       commentDepth = 0;
                                       return new Token("ERROR_COMENTARIO_NO_CERRADO",
                                                        "", commentLine0, commentCol0); }
}

/* EOF normal */
<<EOF>>                              { return null; }
