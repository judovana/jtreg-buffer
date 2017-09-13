/*   Copyright (C) 2017 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation, version 2.

 IcedTea is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with IcedTea; see the file COPYING.  If not, write to
 the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version.
 */
package cryptotest.tests;

import cryptotest.utils.AlgorithmInstantiationException;
import cryptotest.utils.AlgorithmRunException;
import cryptotest.utils.AlgorithmTest;
import cryptotest.utils.TestResult;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.xml.crypto.Data;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.TransformService;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import javax.xml.crypto.dsig.spec.XPathType;
import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/*
 * IwishThisCouldBeAtTest
 */
public class TransformServiceTests extends AlgorithmTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestResult r = new TransformServiceTests().mainLoop();
        System.out.println(r.getExplanation());
        System.out.println(r.toString());
        r.assertItself();
    }

    @Override
    protected void checkAlgorithm(Provider.Service service, String alias) throws
            AlgorithmInstantiationException, AlgorithmRunException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            String xslt
                    = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                    + "   <xsl:template match=\"/\">"
                    + "      <html>"
                    + "         <body>"
                    + "            <p><xsl:value-of select=\"root/child/name\"/></p>"
                    + "            <p><xsl:value-of select=\"root/child/age\"/></p>"
                    + "         </body>"
                    + "      </html>"
                    + "   </xsl:template>"
                    + "</xsl:stylesheet>";
            InputStream xsltStream = new ByteArrayInputStream(xslt.getBytes(StandardCharsets.UTF_8));
            Document document = documentBuilder.parse(xsltStream);

            TransformService ts = TransformService.getInstance(alias, "DOM", service.getProvider());
            final TransformParameterSpec params;
            if (service.getAlgorithm().endsWith("/REC-xslt-19991116")) {
                //Node element = Xml.fakeXml();
                DOMStructure stylesheet = new DOMStructure(document.getDocumentElement());
                XSLTTransformParameterSpec spec = new XSLTTransformParameterSpec(stylesheet);
                params = spec;
            } else if (service.getAlgorithm().endsWith("/xmldsig-filter2")) {
                List<XPathType> list = new ArrayList<>();
                list.add(new XPathType("/", XPathType.Filter.UNION));
                params = new XPathFilter2ParameterSpec(list);
                return;
            } else if (service.getAlgorithm().endsWith("/REC-xpath-19991116")) {
                params = new XPathFilterParameterSpec("/");
                return;
            } else {
                params = null;
                return;
            }
            ts.init(params);

            String xml
                    = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<root>"
                    + "   <child>"
                    + "      <name>name1</name>"
                    + "      <age>23</age>"
                    + "   </child>"
                    + "   <child>"
                    + "      <name>name2</name>"
                    + "      <age>25</age>"
                    + "   </child>"
                    + "</root>";

            InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            Data data = new OctetStreamData(stream);
            DOMStructure s = new DOMStructure(document.getDocumentElement());

            ts.marshalParams(s, null);
            OctetStreamData output = (OctetStreamData) ts.transform(data, null);
            Scanner scan = new Scanner(output.getOctetStream()).useDelimiter("\\A");
            String result = scan.hasNext() ? scan.next() : "";
            System.out.println("output: " + result);

        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException ex) {
            throw new AlgorithmInstantiationException(ex);
        } catch (Exception ex) {
            throw new AlgorithmRunException(ex);
        }

    }

    @Override
    public String getTestedPart() {
        return "TransformService";
    }

}
