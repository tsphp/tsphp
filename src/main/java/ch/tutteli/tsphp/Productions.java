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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A list of productions.
 *
 * @author Robert Stoll <rstoll@tutteli.ch>
 */
public class Productions implements IProductions, Iterable<Production>
{

    private List<Production> productions;
    private Set<String> terminalSymbols = new HashSet<>();
    private Set<String> nonTerminalSymbols = new HashSet<>();

    public Productions() {
        productions = new ArrayList<>();
    }

    public Productions(final List<Production> aProductions) {
        productions = aProductions;
        determineTerminalNonTerminalSymbols();
    }

    private void determineTerminalNonTerminalSymbols() {
        for (Production production : productions) {
            nonTerminalSymbols.add(production.LeftHandSide);
            for (int i = 0; i < production.RightHandSide.length(); ++i) {
                String symbol = production.RightHandSide.substring(i, i + 1);
                if (!nonTerminalSymbols.contains(symbol)) {
                    terminalSymbols.add(symbol);
                }
                if (terminalSymbols.contains(production.LeftHandSide)) {
                    terminalSymbols.remove(production.LeftHandSide);
                }
            }
        }
    }

    @Override
    public final Set<String> getTerminalSymbols() {
        return terminalSymbols;
    }

    @Override
    public final Set<String> getNonTerminalSymbols() {
        return nonTerminalSymbols;
    }

    @Override
    public final int size() {
        return productions.size();
    }

    @Override
    public final Iterator<Production> iterator() {
        return productions.iterator();
    }
}
