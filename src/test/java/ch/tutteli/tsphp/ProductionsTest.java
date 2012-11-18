/*
 * Copyright 2012 Robert Stoll <rstoll@tutteli.ch>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package ch.tutteli.tsphp;

import static ch.tutteli.tsphp.CollectionHelper.arrayToSet;
import java.util.Arrays;
import java.util.HashSet;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Robert Stoll <rstoll@tutteli.ch>
 */
public class ProductionsTest {

    @Test
    public void testNoProduction() {
        Productions productions = new Productions();
        assertEquals(0, productions.getNonTerminalSymbols().size());
        assertEquals(0, productions.getTerminalSymbols().size());

        //since determination of terminal and non terminal symbols is executed only once, we want to see if the order has an affect
        productions = new Productions();
        assertEquals(0, productions.getTerminalSymbols().size());
        assertEquals(0, productions.getNonTerminalSymbols().size());
    }

    @Test
    public void testOnlyEmptyStringProductions() {
        Productions productions = new Productions(Arrays.asList(
                new Production("S", Production.EMPTY_STRING),
                new Production("E", Production.EMPTY_STRING),
                new Production("F", Production.EMPTY_STRING),
                new Production("H", Production.EMPTY_STRING),
                new Production("I", Production.EMPTY_STRING)));
        assertEquals(arrayToSet(new String[]{"S","E","F","H","I"}),productions.getNonTerminalSymbols());
        assertEquals(new HashSet<String>(),productions.getTerminalSymbols());
    }

    @Test
    public void testDoubleNonTerminalSymbol() {
        Productions productions = new Productions(Arrays.asList(
                new Production("S", Production.EMPTY_STRING),
                new Production("S", "a")));
        assertEquals(arrayToSet(new String[]{"S"}),productions.getNonTerminalSymbols());
        assertEquals(arrayToSet(new String[]{"a"}),productions.getTerminalSymbols());
    }

    @Test
    public void testDoubleTerminalSymbols() {
        Productions productions = new Productions(Arrays.asList(
                new Production("S", "a"),
                new Production("E", "a")));
        assertEquals(arrayToSet(new String[]{"S","E"}),productions.getNonTerminalSymbols());
        assertEquals(arrayToSet(new String[]{"a"}),productions.getTerminalSymbols());
    }

    @Test
    public void testNonTerminalToNonTerminalSymbol() {
        Productions productions = new Productions(Arrays.asList(
                new Production("S", "E"),
                new Production("E", "a"),
                new Production("F", "E")));
        assertEquals(arrayToSet(new String[]{"S","E","F"}),productions.getNonTerminalSymbols());
        assertEquals(arrayToSet(new String[]{"a"}),productions.getTerminalSymbols());
    }

    @Test
    public void testGrammar2_48CompilersPrinciplesTechniquesAndTools() {
        Productions productions = new Productions(Arrays.asList(
                new Production("E", "TD"),
                new Production("D", "+TD"),
                new Production("D", Production.EMPTY_STRING),
                new Production("T", "FU"),
                new Production("U", "*FU"),
                new Production("U", Production.EMPTY_STRING),
                new Production("F", "(E)"),
                new Production("F", "i")));
        assertEquals(arrayToSet(new String[]{"E","D","T","U","F"}),productions.getNonTerminalSymbols());
        assertEquals(arrayToSet(new String[]{"+","*","(",")","i"}),productions.getTerminalSymbols());
    }

    @Test
    public void testSize() {
        Productions productions = new Productions(Arrays.asList(
                new Production("S", Production.EMPTY_STRING),
                new Production("E", "F"),
                new Production("F", Production.EMPTY_STRING),
                new Production("H", "a")));
        assertEquals(4, productions.size());
    }

    @Test
    public void testIterator() {
        Production[] productions1 = new Production[]{
            new Production("S", Production.EMPTY_STRING),
            new Production("E", "F"),
            new Production("F", Production.EMPTY_STRING),
            new Production("H", "a")
        };
        Productions productions2 = new Productions(Arrays.asList(productions1));

        int i = 0;
        for (Production production : productions2) {
            assertEquals(production, productions1[i]);
            ++i;
        }
    }
    @Test
    public void testGrammarWithLongRightHandSide() {
        Productions productions = new Productions(Arrays.asList(
                new Production("E", "ABCDEz"),
                new Production("A", "x"),
                new Production("B", "C"),
                new Production("B", Production.EMPTY_STRING),
                new Production("C", "(A)"),
                new Production("C", Production.EMPTY_STRING),
                new Production("D", "y"),
                new Production("D", Production.EMPTY_STRING)));
        assertEquals(arrayToSet(new String[]{"E","A","B","C","D"}),productions.getNonTerminalSymbols());
        assertEquals(arrayToSet(new String[]{"x","(",")","y","z"}),productions.getTerminalSymbols());        
    }
    
}
