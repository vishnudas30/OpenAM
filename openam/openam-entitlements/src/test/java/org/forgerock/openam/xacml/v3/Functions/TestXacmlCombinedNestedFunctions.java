/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
 ~
 ~ The contents of this file are subject to the terms
 ~ of the Common Development and Distribution License
 ~ (the License). You may not use this file except in
 ~ compliance with the License.
 ~
 ~ You can obtain a copy of the License at
 ~ http://forgerock.org/license/CDDLv1.0.html
 ~ See the License for the specific language governing
 ~ permission and limitations under the License.
 ~
 ~ When distributing Covered Code, include this CDDL
 ~ Header Notice in each file and include the License file
 ~ at http://forgerock.org/license/CDDLv1.0.html
 ~ If applicable, add the following below the CDDL Header,
 ~ with the fields enclosed by brackets [] replaced by
 ~ your own identifying information:
 ~ "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.openam.xacml.v3.Functions;

import org.forgerock.openam.xacml.v3.model.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * A.3.16 Other functions
 urn:oasis:names:tc:xacml:3.0:function:access-permitted
 This function SHALL take an “http://www.w3.org/2001/XMLSchema#anyURI” and an
 "http://www.w3.org/2001/XMLSchema#string" as arguments.
 The first argument SHALL be interpreted as an attribute category.
 The second argument SHALL be interpreted as the XML content of an <Attributes> element with Category
 equal to the first argument. The function evaluates to an "http://www.w3.org/2001/XMLSchema#boolean".
 This function SHALL return "True" if and only if the policy evaluation described below returns the value of "Permit".

 The following evaluation is described as if the context is actually instantiated,
 but it is only required that an equivalent result be obtained.

 The function SHALL construct a new context, by copying all the information from the current context,
 omitting any <Attributes> element with Category equal to the first argument.

 The second function argument SHALL be added to the context as the content of an <Attributes> element
 with Category equal to the first argument.

 The function SHALL invoke a complete policy evaluation using the newly constructed context.
 This evaluation SHALL be completely isolated from the evaluation which invoked the function,
 but shall use all current policies and combining algorithms, including any per request policies.

 The PDP SHALL detect any loop which may occur if successive evaluations invoke
 this function by counting the number of total invocations of any instance of this function during
 any single initial invocation of the PDP. If the total number of invocations exceeds the bound for such
 invocations, the initial invocation of this function evaluates to Indeterminate with a
 “urn:oasis:names:tc:xacml:1.0:status:processing-error” status code.

 Also, see the security considerations in section 9.1.8.
 */

/**
 * XACML Other Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlCombinedNestedFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");

    static final FunctionArgument testString1 = new DataValue(DataType.XACMLSTRING, "Forge");
    static final FunctionArgument testString2 = new DataValue(DataType.XACMLSTRING, "Rock");
    static final FunctionArgument testString3 = new DataValue(DataType.XACMLSTRING, " says Hello World!");
    static final FunctionArgument testString4 = new DataValue(DataType.XACMLSTRING, " HELLO WORLD!");

    FunctionArgument testInteger1 = new DataValue(DataType.XACMLINTEGER, 195, true);

    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     * Test a Combined Nested set of Functions
     */
    @Test
    public void test_UseCase_Combined_Nested_Functions() throws XACML3EntitlementException {
        StringBag stringBag = new StringBag();

        StringConcatenate stringConcatenate = new StringConcatenate();
        // Place Objects in Argument stack.
        stringConcatenate.addArgument(testString1);
        stringConcatenate.addArgument(testString2);
        stringConcatenate.addArgument(testString3);
        stringConcatenate.addArgument(testString4);

        StringFromInteger stringFromInteger = new StringFromInteger();
        stringFromInteger.addArgument(testInteger1);

        StringFromBoolean stringFromBoolean = new StringFromBoolean();
        stringFromBoolean.addArgument(trueObject);
        StringFromBoolean stringFromBoolean2 = new StringFromBoolean();
        stringFromBoolean2.addArgument(falseObject);

        stringBag.addArgument(stringConcatenate);
        stringBag.addArgument(stringFromInteger);
        stringBag.addArgument(stringFromBoolean);
        stringBag.addArgument(stringFromBoolean2);

        // Trigger Evaluation
        DataBag dataBag = (DataBag) stringBag.evaluate(null);
        // Check raw Result
        assertNotNull(dataBag);
        assertEquals(dataBag.size(), 4);

        // Check native unwrapped Result
        List<String> collection = TestXacmlDataUtils.asStringCollection(dataBag);
        assertNotNull(collection);
        assertEquals(collection.size(), 4);
        assertEquals(collection.get(0),"ForgeRock says Hello World! HELLO WORLD!");
        assertEquals(collection.get(1),"195");
        assertEquals(collection.get(2),"true");
        assertEquals(collection.get(3),"false");

    }

}
