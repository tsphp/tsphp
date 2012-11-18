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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 *
 * @author Robert Stoll <rstoll@tutteli.ch>
 */
public class DummyForJenkinsTest {

    @Test
    public void testDefaultConstructor() {
        DummyForJenkins dummy = new DummyForJenkins();
        assertNull(dummy.getName());
    }
    
    @Test
    public void testParameterisedConstructor() {
        DummyForJenkins dummy = new DummyForJenkins("Robert");
        assertEquals("Robert", dummy.getName());
    }
    
    @Test
    public void testSetName() {
        DummyForJenkins dummy = new DummyForJenkins("Robert");
        dummy.setName("Peter");
        assertEquals("Peter", dummy.getName());
    }
}
