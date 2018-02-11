package Parser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Token {
    int errorToken = 0;
    int timesToken = 1; // *
    int divToken = 2; ///

    int plusToken = 11; // +
    int minusToken = 12; // -

    int eqlToken = 20; // ==
    int neqToken = 21; // !=
    int lssToken = 22; // <
    int geqToken = 23; // >=
    int leqToken = 24; // <=
    int gtrToken = 25; // >

    int periodToken = 30; // .
    int commaToken = 31; // ,
    int openbracketToken = 32; // [
    int closebracketToken = 34; // ]
    int closeparenToken = 35; // )

    int becomesToken = 40; // <-
    int thenToken = 41; // then
    int doToken = 42; // do
    int openparenToken = 50; // (

    int number = 60;
    int ident = 61;

    int semiToken = 70; // ;

    int endToken = 80; // }
    int odToken = 81; // od
    int fiToken = 82; // fi

    int elseToken = 90; // else

    int letToken = 100; // let
    int callToken = 101; // call
    int ifToken = 102; // if
    int whileToken = 103; // while
    int returnToken = 104; // return

    int varToken = 110; // variable
    int arrToken = 111; // array
    int funcToken = 112; // function
    int procToken = 113; // procedure

    int beginToken = 150; // {
    int mainToken = 200; // main
    int eofToken = 255; // EOF


    Map<String, Integer> tokenValueMap = Map.ofEntries(
            Map.entry("*", timesToken),
            Map.entry("/", divToken),
            Map.entry("+", plusToken),
            Map.entry("-", minusToken),
            Map.entry("==", eqlToken),
            Map.entry("!=", neqToken),
            Map.entry("<", lssToken),
            Map.entry(">=", geqToken),
            Map.entry("<=", leqToken),
            Map.entry(">", gtrToken),
            Map.entry(".", periodToken),
            Map.entry(",", commaToken),
            Map.entry("[", openbracketToken),
            Map.entry("]",closebracketToken),
            Map.entry(")", closeparenToken),
            Map.entry("<-", becomesToken),
            Map.entry("then", thenToken),
            Map.entry("do", doToken),
            Map.entry("(", openparenToken),
            Map.entry(";", semiToken),
            Map.entry("}", endToken),
            Map.entry("od", odToken),
            Map.entry("fi", fiToken),
            Map.entry("else", elseToken),
            Map.entry("let", letToken),
            Map.entry("call", callToken),
            Map.entry("if", ifToken),
            Map.entry("while", whileToken),
            Map.entry("return", returnToken),
            Map.entry("var", varToken),
            Map.entry("array", arrToken),
            Map.entry("function", funcToken),
            Map.entry("procedure", procToken),
            Map.entry("{", beginToken),
            Map.entry("main", mainToken)
    );

    Set<String> keywords = Set.of("then", "do", "od", "fi", "else", "let", "call", "if",
            "while", "return", "var", "array", "function", "procedure", "main");

    Set<Character> whitespaces = Set.of(' ', '\n', '\t', '\r');
    Set<Character> singleTokens = Set.of('(', '{', '[', ')', '}', ']', ';', ',', '.');
    Set<String> comments = Set.of("//", "#");
}
