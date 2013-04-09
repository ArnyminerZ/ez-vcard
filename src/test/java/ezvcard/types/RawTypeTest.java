package ezvcard.types;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.HtmlUtils;
import ezvcard.util.JCardDataType;
import ezvcard.util.JCardValue;
import ezvcard.util.XCardElement;

/*
 Copyright (c) 2013, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
public class RawTypeTest {
	final List<String> warnings = new ArrayList<String>();
	final CompatibilityMode compatibilityMode = CompatibilityMode.RFC;
	final VCardSubTypes subTypes = new VCardSubTypes();
	final String propertyValue = "value;value";
	final String propertyValueEscaped = "value\\;value";
	final RawTypeImpl type = new RawTypeImpl(propertyValue);

	@After
	public void after() {
		warnings.clear();
	}

	@Test
	public void marshalText() {
		String actual = type.marshalText(VCardVersion.V2_1, warnings, compatibilityMode);
		assertEquals(propertyValue, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalXml() {
		XCardElement xe = new XCardElement(RawTypeImpl.NAME.toLowerCase());
		xe.append("unknown", propertyValue);
		Document expected = xe.document();
		xe = new XCardElement(RawTypeImpl.NAME.toLowerCase());
		Document actual = xe.document();
		type.marshalXml(xe.element(), VCardVersion.V4_0, warnings, compatibilityMode);
		assertXMLEqual(expected, actual);
		assertEquals(0, warnings.size());
	}

	@Test
	public void marshalJson() {
		JCardValue value = type.marshalJson(VCardVersion.V4_0, new ArrayList<String>());
		assertEquals(JCardDataType.TEXT, value.getDataType());
		assertFalse(value.isStructured());

		//@formatter:off
		@SuppressWarnings("unchecked")
		List<List<Object>> expectedValues = Arrays.asList(
			Arrays.asList(new Object[]{ propertyValue })
		);
		//@formatter:on
		assertEquals(expectedValues, value.getValues());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalText() {
		RawTypeImpl type = new RawTypeImpl();
		type.unmarshalText(subTypes, propertyValue, VCardVersion.V2_1, warnings, compatibilityMode);

		assertEquals(propertyValue, type.getValue());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalXml() {
		RawTypeImpl type = new RawTypeImpl();
		XCardElement xe = new XCardElement(RawTypeImpl.NAME.toLowerCase());
		xe.text(propertyValue);
		xe.text("another value");
		Element input = xe.element();
		type.unmarshalXml(subTypes, input, VCardVersion.V4_0, warnings, compatibilityMode);

		assertEquals(propertyValue, type.getValue());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalXml_no_child_elements() {
		RawTypeImpl type = new RawTypeImpl();
		XCardElement xe = new XCardElement(RawTypeImpl.NAME.toLowerCase());
		xe.element().setTextContent(propertyValue);
		Element input = xe.element();
		type.unmarshalXml(subTypes, input, VCardVersion.V4_0, warnings, compatibilityMode);

		assertEquals(propertyValue, type.getValue());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalXml_no_text_content() {
		RawTypeImpl type = new RawTypeImpl();
		XCardElement xe = new XCardElement(RawTypeImpl.NAME.toLowerCase());
		Element input = xe.element();
		type.unmarshalXml(subTypes, input, VCardVersion.V4_0, warnings, compatibilityMode);

		assertEquals("", type.getValue());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalHtml() throws Exception {
		org.jsoup.nodes.Element element = HtmlUtils.toElement("<div>" + propertyValue + "</div>");

		RawTypeImpl type = new RawTypeImpl();
		type.unmarshalHtml(element, warnings);

		assertEquals(propertyValue, type.getValue());
		assertEquals(0, warnings.size());
	}

	@Test
	public void unmarshalJson() {
		JCardValue value = new JCardValue();
		value.setDataType(JCardDataType.TEXT);
		value.addValues(propertyValue);

		RawTypeImpl type = new RawTypeImpl();
		type.unmarshalJson(subTypes, value, VCardVersion.V4_0, warnings);

		assertEquals(propertyValueEscaped, type.getValue());
		assertEquals(0, warnings.size());
	}

	private class RawTypeImpl extends RawType {
		public static final String NAME = "RAW";

		public RawTypeImpl() {
			super(NAME);
		}

		public RawTypeImpl(String value) {
			super(NAME, value);
		}
	}
}