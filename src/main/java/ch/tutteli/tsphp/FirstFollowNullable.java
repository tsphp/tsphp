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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * First, follow, nullable algorithm adapted from the book modern compiler implementation in java.
 *
 * @author Robert Stoll <rstoll@tutteli.ch>
 */
public class FirstFollowNullable
{

    private Map<String, Set<String>> first;
    private Map<String, Set<String>> follow;
    private Map<String, Boolean> nullable;
    private Productions productions;
    private boolean dataChanged = true;

    public final Map<String, Set<String>> getFirst() {
        return first;
    }

    public final Map<String, Set<String>> getFollow() {
        return follow;
    }

    public final Map<String, Boolean> getNullable() {
        return nullable;
    }

    public final void run(final Productions aProductions) {
        productions = aProductions;
        reset();
        setFirstOfTerminals();
        while (dataChanged) {
            dataChanged = false;
            for (Production production : productions) {

                if (areAllSymbolsNullable(production.RightHandSide)) {
                    if (!dataChanged && !nullable.get(production.LeftHandSide)) {
                        dataChanged = true;
                    }
                    nullable.put(production.LeftHandSide, true);
                }
                computeFirstFollow(production);
            }
        }
    }

    private void reset() {
        Set<String> nonTerminalSymbols = productions.getNonTerminalSymbols();
        Set<String> terminalSymbols = productions.getTerminalSymbols();
        int size = nonTerminalSymbols.size() + terminalSymbols.size();
        first = new HashMap<>(size);
        follow = new HashMap<>(size);
        nullable = new HashMap<>(size);

        for (String key : nonTerminalSymbols) {
            first.put(key, new HashSet<String>());
            follow.put(key, new HashSet<String>());
            nullable.put(key, Boolean.FALSE);
        }
        for (String key : terminalSymbols) {
            first.put(key, new HashSet<String>());
            follow.put(key, new HashSet<String>());
            nullable.put(key, Boolean.FALSE);
        }
    }

    private void setFirstOfTerminals() {
        for (String terminal : productions.getTerminalSymbols()) {
            Set set = new HashSet();
            set.add(terminal);
            first.put(terminal, set);
        }
    }

    private void computeFirstFollow(final Production production) {

        int rightHandSideLenght = production.RightHandSide.length();

        for (int i = 0; i < rightHandSideLenght; ++i) {

            //for instance, Z -> XY, i=1 and X is nullable, then First(Y) can be added to First(Z)
            if (arePrecedingSymbolsNullableOrIsThereNoPrecedingSymbol(production.RightHandSide, i)) {
                String symbol = production.RightHandSide.substring(i, i + 1);
                int size = first.get(production.LeftHandSide).size();
                first.get(production.LeftHandSide).addAll(first.get(symbol));
                if (!dataChanged && size != first.get(production.LeftHandSide).size()) {
                    dataChanged = true;
                }
            }

            //for instance,A->ZB, Z -> XY, i=0 and Y is nullable, then Follow(Z) can be added to Follow(X). 
            //That means B will also be in Follow(X)
            if (areFollowingSymbolsNullableOrIsThereNoFollowingSymbol(production.RightHandSide, i)) {
                String symbol = production.RightHandSide.substring(i, i + 1);
                int size = follow.get(symbol).size();
                follow.get(symbol).addAll(follow.get(production.LeftHandSide));
                if (!dataChanged && size != follow.get(symbol).size()) {
                    dataChanged = true;
                }
            }


            for (int j = i + 1; j < rightHandSideLenght; ++j) {
                //for instance, Z -> XYZ, Follow(X)=First(Y), Follow(X)=First(Z) if Y is nullable
                if (areSymbolsInbetweenSymbolAndSymbol2NullableOrAreThereNoSymbolsInbetween(production.RightHandSide,
                        i, j)) {
                    String symbol = production.RightHandSide.substring(i, i + 1);
                    String symbol2 = production.RightHandSide.substring(j, j + 1);
                    int size = follow.get(symbol).size();
                    follow.get(symbol).addAll(first.get(symbol2));
                    if (!dataChanged && size != follow.get(symbol).size()) {
                        dataChanged = true;
                    }
                }
            }
        }
    }

    private boolean areAllSymbolsNullable(final String symbols) {
        boolean areAllNullable = true;
        for (int i = 0; i < symbols.length(); ++i) {
            if (!nullable.get(symbols.substring(i, i + 1))) {
                areAllNullable = false;
                break;
            }
        }
        return areAllNullable;
    }

    private boolean arePrecedingSymbolsNullableOrIsThereNoPrecedingSymbol(final String symbols,
            final int symbolOffset) {
        return areAllSymbolsNullable(symbols.substring(0, symbolOffset));
    }

    private boolean areFollowingSymbolsNullableOrIsThereNoFollowingSymbol(final String symbols,
            final int symbolOffset) {
        return areAllSymbolsNullable(symbols.substring(symbolOffset + 1));
    }

    private boolean areSymbolsInbetweenSymbolAndSymbol2NullableOrAreThereNoSymbolsInbetween(final String symbols,
            final int symbol1Offset, final int symbol2Offset) {
        boolean noSymbolInbetween = symbol1Offset + 1 == symbol2Offset;
        return noSymbolInbetween || areAllSymbolsNullable(symbols.substring(symbol1Offset + 1, symbol2Offset));
    }
}
