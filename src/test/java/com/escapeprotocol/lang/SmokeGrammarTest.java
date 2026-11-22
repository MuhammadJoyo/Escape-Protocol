package com.escapeprotocol.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

/** Verifies that the ANTLR-generated parser exists, compiles, and runs. */
class SmokeGrammarTest {

    private static int countSyntaxErrors(String source) {
        SmokeLexer lexer = new SmokeLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        SmokeParser parser = new SmokeParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.program();
        return parser.getNumberOfSyntaxErrors();
    }

    @Test
    void validInputParsesWithoutErrors() {
        assertEquals(0, countSyntaxErrors("ping door; ping lab;"));
    }

    @Test
    void invalidInputReportsSyntaxError() {
        assertTrue(countSyntaxErrors("ping ;") > 0);
    }
}