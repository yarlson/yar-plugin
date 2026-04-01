package dev.yarlson.yar.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static dev.yarlson.yar.psi.YarTypes.*;

%%

%class YarLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

WHITE_SPACE=[\s]+
LINE_COMMENT="//"[^\r\n]*
INTEGER_LITERAL=[0-9]+
IDENTIFIER=[a-zA-Z_][a-zA-Z0-9_]*
STRING_LITERAL=\"([^\"\\\r\n]|\\[ntr0\\\"])*\"
UNTERMINATED_STRING=\"([^\"\\\r\n]|\\[ntr0\\\"])*
CHAR_LITERAL='([^'\\\r\n]|\\[ntr0\\\\'])'
UNTERMINATED_CHAR='([^'\\\r\n]|\\[ntr0\\\\'])?

%%

<YYINITIAL> {
  {WHITE_SPACE}           { return WHITE_SPACE; }
  {LINE_COMMENT}          { return LINE_COMMENT; }

  // Keywords
  "package"               { return PACKAGE_KW; }
  "import"                { return IMPORT_KW; }
  "fn"                    { return FN_KW; }
  "pub"                   { return PUB_KW; }
  "var"                   { return VAR_KW; }
  "let"                   { return LET_KW; }
  "struct"                { return STRUCT_KW; }
  "interface"             { return INTERFACE_KW; }
  "enum"                  { return ENUM_KW; }
  "or"                    { return OR_KW; }
  "if"                    { return IF_KW; }
  "else"                  { return ELSE_KW; }
  "for"                   { return FOR_KW; }
  "break"                 { return BREAK_KW; }
  "continue"              { return CONTINUE_KW; }
  "return"                { return RETURN_KW; }
  "match"                 { return MATCH_KW; }
  "case"                  { return CASE_KW; }
  "taskgroup"             { return TASKGROUP_KW; }
  "spawn"                 { return SPAWN_KW; }
  "true"                  { return TRUE_KW; }
  "false"                 { return FALSE_KW; }
  "nil"                   { return NIL_KW; }
  "error"                 { return ERROR_KW; }
  "map"                   { return MAP_KW; }
  "chan"                  { return CHAN_KW; }

  // Multi-character operators (must come before single-char)
  ":="                    { return COLON_ASSIGN; }
  "+="                    { return PLUS_EQ; }
  "-="                    { return MINUS_EQ; }
  "*="                    { return STAR_EQ; }
  "/="                    { return SLASH_EQ; }
  "%="                    { return PERCENT_EQ; }
  "=="                    { return EQ_EQ; }
  "!="                    { return BANG_EQ; }
  "<="                    { return LT_EQ; }
  ">="                    { return GT_EQ; }
  "&&"                    { return AMP_AMP; }
  "||"                    { return PIPE_PIPE; }

  // Single-character operators and delimiters
  "="                     { return EQ; }
  ":"                     { return COLON; }
  ";"                     { return SEMICOLON; }
  "!"                     { return BANG; }
  "?"                     { return QUESTION; }
  ","                     { return COMMA; }
  "."                     { return DOT; }
  "&"                     { return AMP; }
  "|"                     { return PIPE; }
  "+"                     { return PLUS; }
  "-"                     { return MINUS; }
  "*"                     { return STAR; }
  "/"                     { return SLASH; }
  "%"                     { return PERCENT; }
  "<"                     { return LT; }
  ">"                     { return GT; }
  "("                     { return LPAREN; }
  ")"                     { return RPAREN; }
  "{"                     { return LBRACE; }
  "}"                     { return RBRACE; }
  "["                     { return LBRACKET; }
  "]"                     { return RBRACKET; }

  // Literals
  {STRING_LITERAL}        { return STRING_LITERAL; }
  {UNTERMINATED_STRING}   { return BAD_CHARACTER; }
  {CHAR_LITERAL}          { return CHAR_LITERAL; }
  {UNTERMINATED_CHAR}     { return BAD_CHARACTER; }
  {INTEGER_LITERAL}       { return INTEGER_LITERAL; }

  // Identifier (after keywords)
  {IDENTIFIER}            { return IDENTIFIER; }
}

[^]                       { return BAD_CHARACTER; }
