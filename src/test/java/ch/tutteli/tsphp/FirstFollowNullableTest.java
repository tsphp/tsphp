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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Robert Stoll <rstoll@tutteli.ch>
 */
public class FirstFollowNullableTest {

    private FirstFollowNullable firstFollowNullable;

    @Before
    public void setUp() {
        firstFollowNullable = new FirstFollowNullable();
    }

    @Test
    public void testInitialisation() {
        assertEquals(null, firstFollowNullable.getFirst());
        assertEquals(null, firstFollowNullable.getFollow());
        assertEquals(null, firstFollowNullable.getNullable());
    }

    @Test
    public void testGrammarWithLongRightHandSide() {
        Productions productions = new Productions(Arrays.asList(
                new Production("E", "ABCDE"),
                new Production("A", "x"),
                new Production("B", "C"),
                new Production("B", Production.EMPTY_STRING),
                new Production("C", "(A)"),
                new Production("C", Production.EMPTY_STRING),
                new Production("D", "y"),
                new Production("D", Production.EMPTY_STRING)));
        firstFollowNullable.run(productions);
        Map<String, Set<String>> expectedFirst = convertToFirstFollowMap(
                new String[][][]{
                    {{"E"}, {"x"}},
                    {{"A"}, {"x"}},
                    {{"B"}, {"("}},
                    {{"C"}, {"("}},
                    {{"D"}, {"y"}},
                    {{"("}, {"("}},
                    {{")"}, {")"}},
                    {{"x"}, {"x"}},
                    {{"y"}, {"y"}},
                });

        assertEquals(expectedFirst, firstFollowNullable.getFirst());

        Map<String, Set<String>> expectedFollow = convertToFirstFollowMap(
                new String[][][]{
                    {{"E"}, {}},
                    {{"A"}, {"(", ")", "y", "x"}},
                    {{"B"}, {"(", "y", "x"}},
                    {{"C"}, {"(", "y", "x"}},
                    {{"D"}, {"x"}},
                    {{"("}, {"x"}},
                    {{")"}, {"(", "y", "x"}},
                    {{"x"}, {"(", ")", "y", "x"}},
                    {{"y"}, {"x"}},
                });
        assertEquals(expectedFollow, firstFollowNullable.getFollow());

        Map<String, Boolean> expectedNullable = convertToNullableMap(
                new String[]{"E", "A", "B", "C", "D", "(", ")", "x", "y"},
                new Boolean[]{false, false, true, true, true, false, false, false, false});

        assertEquals(expectedNullable, firstFollowNullable.getNullable());
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
        firstFollowNullable.run(productions);
        Map<String, Set<String>> expectedFirst = convertToFirstFollowMap(new String[][][]{
                    {{"E"}, {"(", "i"}},
                    {{"D"}, {"+"}},
                    {{"T"}, {"(", "i"}},
                    {{"U"}, {"*"}},
                    {{"F"}, {"(", "i"}},
                    {{"+"}, {"+"}},
                    {{"*"}, {"*"}},
                    {{"("}, {"("}},
                    {{")"}, {")"}},
                    {{"i"}, {"i"}}
                });

        assertEquals(expectedFirst, firstFollowNullable.getFirst());

        Map<String, Set<String>> expectedFollow = convertToFirstFollowMap(
                new String[][][]{
                    {{"E"}, {")"}},
                    {{"D"}, {")"}},
                    {{"T"}, {"+", ")"}},
                    {{"U"}, {"+", ")"}},
                    {{"F"}, {"+", ")", "*"}},
                    {{"+"}, {"(", "i"}},
                    {{"*"}, {"(", "i"}},
                    {{"("}, {"(", "i"}},
                    {{")"}, {"*", "+", ")"}},
                    {{"i"}, {"*", "+", ")"}}
                });
        assertEquals(expectedFollow, firstFollowNullable.getFollow());

        Map<String, Boolean> expectedNullable = convertToNullableMap(
                new String[]{"E", "D", "T", "U", "F", "+", "*", "(", ")", "i"},
                new Boolean[]{false, true, false, true, false, false, false, false, false, false});

        assertEquals(expectedNullable, firstFollowNullable.getNullable());
    }

    @Test
    public void testGrammar3_12ModernCompilerImplementationInJava() {
        Productions productions = new Productions(Arrays.asList(
                new Production("Z", "d"),
                new Production("Z", "XYZ"),
                new Production("Y", Production.EMPTY_STRING),
                new Production("Y", "c"),
                new Production("X", "Y"),
                new Production("X", "a")));
        firstFollowNullable.run(productions);

        Map<String, Set<String>> expectedFirst = convertToFirstFollowMap(new String[][][]{
                    {{"X"}, {"a", "c"}},
                    {{"Y"}, {"c"}},
                    {{"Z"}, {"a", "c", "d"}},
                    {{"a"}, {"a"}},
                    {{"c"}, {"c"}},
                    {{"d"}, {"d"}}
                });
        assertEquals(expectedFirst, firstFollowNullable.getFirst());

        Map<String, Set<String>> expectedFollow = convertToFirstFollowMap(
                new String[][][]{
                    {{"X"}, {"a", "c", "d"}},
                    {{"Y"}, {"a", "c", "d"}},
                    {{"Z"}, {}},
                    {{"a"}, {"a", "c", "d"}},
                    {{"c"}, {"a", "c", "d"}},
                    {{"d"}, {}}
                });
        assertEquals(expectedFollow, firstFollowNullable.getFollow());

        Map<String, Boolean> expectedNullable = convertToNullableMap(
                new String[]{"X", "Y", "Z", "a", "c", "d"},
                new Boolean[]{true, true, false, false, false, false});
        assertEquals(expectedNullable, firstFollowNullable.getNullable());
    }

    @Test
    public void testOnlyEmptyProductions() {
        Productions productions = new Productions(Arrays.asList(
                new Production("S", Production.EMPTY_STRING),
                new Production("E", Production.EMPTY_STRING),
                new Production("F", Production.EMPTY_STRING),
                new Production("H", Production.EMPTY_STRING),
                new Production("I", Production.EMPTY_STRING)));
        firstFollowNullable.run(productions);
        Map<String, Set<String>> expectedFirstAndFollow = new HashMap<>();
        expectedFirstAndFollow.put("S", new HashSet<String>());
        expectedFirstAndFollow.put("E", new HashSet<String>());
        expectedFirstAndFollow.put("F", new HashSet<String>());
        expectedFirstAndFollow.put("H", new HashSet<String>());
        expectedFirstAndFollow.put("I", new HashSet<String>());

        assertEquals(expectedFirstAndFollow, firstFollowNullable.getFirst());
        assertEquals(expectedFirstAndFollow, firstFollowNullable.getFollow());

        Map<String, Boolean> expectedNullable = new HashMap<>();
        expectedNullable.put("S", true);
        expectedNullable.put("E", true);
        expectedNullable.put("F", true);
        expectedNullable.put("H", true);
        expectedNullable.put("I", true);
        assertEquals(expectedNullable, firstFollowNullable.getNullable());
    }

    private Map<String, Set<String>> convertToFirstFollowMap(String[][][] array) {
        Map<String, Set<String>> expected = new HashMap<>();
        for (int i = 0; i < array.length; ++i) {
            Set<String> symbols = new HashSet<>();
            for (int j = 0; j < array[i][1].length; ++j) {
                symbols.add(array[i][1][j]);
            }
            expected.put(array[i][0][0], symbols);
        }
        return expected;
    }

    private Map<String, Boolean> convertToNullableMap(String[] symbolsLeftHandSide, Boolean[] nullables) {
        Map<String, Boolean> expected = new HashMap<>();
        for (int i = 0; i < symbolsLeftHandSide.length; ++i) {
            expected.put(symbolsLeftHandSide[i], nullables[i]);
        }
        return expected;
    }
}
