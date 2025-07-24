<#function _ ident msg>
  <#return msgResolver.getMessage(ident, msg)/>
</#function>

<#macro pdf title default>
<?xml version="1.0" encoding="UTF-8"?>
<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <fo:layout-master-set>
    <fo:simple-page-master master-name="A4" page-height="29.7cm" page-width="21.0cm" margin="2cm">
      <fo:region-body margin-top="3cm" space-after="4cm"/>
      <fo:region-before region-name="header"/>
      <fo:region-after region-name="footer" margin-bottom="5cm" extent="4cm"/>
    </fo:simple-page-master>
  </fo:layout-master-set>

  <fo:page-sequence master-reference="A4" initial-page-number="1">
    <fo:title>
      ${_(title, default)}
    </fo:title>

    <fo:static-content flow-name="header">
      <fo:table table-layout="fixed" width="100%" display-align="center">
        <fo:table-column column-width="75%"/>
        <fo:table-column column-width="25%"/>

        <fo:table-body>
          <fo:table-row>
            <fo:table-cell>
              <fo:block>
                <fo:external-graphic src="url('iish_logo_en.jpg')" content-height="40px"/>
              </fo:block>
            </fo:table-cell>

            <fo:table-cell text-align="left" color="#5e92c2" font-size="0.6em" margin-right="1cm">
              <fo:block>
                ${_("reproductionInvoice.instituteInfo", "An institute of the Royal Netherlands Academy of Arts and Sciences (KNAW)")}
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
        </fo:table-body>
      </fo:table>
    </fo:static-content>

    <fo:static-content flow-name="footer">
      <!-- Payment information -->
      <fo:table table-layout="fixed" width="100%" display-align="center" font-size="0.6em" space-before="0.5cm">
        <fo:table-column column-width="3cm"/>
        <fo:table-column/>

        <fo:table-body>
          <fo:table-row>
            <fo:table-cell margin-left="5%">
              <fo:block>
                ${_("reproductionInvoice.cocNumber", "CoC number")}:
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.cocNumberDesc", "54667089")}
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
          <fo:table-row>
            <fo:table-cell margin-left="5%">
              <fo:block>
                ${_("reproductionInvoice.vatNumberBottom", "VAT number")}:
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.vatNumberDesc", "NL002958697B01")}
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
        </fo:table-body>
      </fo:table>

      <!-- General Terms -->
      <fo:table table-layout="fixed" width="100%" display-align="center" font-size="0.6em" space-before="0.5cm">
        <fo:table-column/>

        <fo:table-body>
          <fo:table-row>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.terms" "The KNAW is under the Sales Tax Act 1968 c.a. exempt from sales tax in respect of government
                    designated services and supplies of social and cultural nature, including research. On supplies and services of the
                    KNAW which are not subject to that, tax is calculated.")}
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
        </fo:table-body>
      </fo:table>

      <!-- IISG General info -->
      <fo:table table-layout="fixed" width="100%" display-align="center" font-size="0.6em" space-before="0.5cm">
        <fo:table-column column-width="5%"/>
        <fo:table-column column-width="15%"/>
        <fo:table-column column-width="8%"/>
        <fo:table-column column-width="15%"/>
        <fo:table-column column-width="8%"/>
        <fo:table-column column-width="15%"/>
        <fo:table-column column-width="8%"/>
        <fo:table-column column-width="23%"/>
        <fo:table-column column-width="8%"/>

        <fo:table-body>
          <fo:table-row>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.visiting", "Visiting Address")}:
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.postal", "Postal Address")}:
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.telephone", "t +31(0)206685866")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.bankHolder", "bankHolder")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
          <!-- Second row! -->
          <fo:table-row>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.visitingAddress", "Cruquiusweg 31 1019")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.postalAddress", "P.O. Box 2169 1000 CD Amsterdam The Netherlands")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.fax", "f +31(0)206654181")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.bankInfo", "bankInfo")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
          <!-- Third row -->
          <fo:table-row>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.visitingCity", "AT Amsterdam")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.postalCity", "1000 CD Amsterdam")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.mail", "info@iisg.nl")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.bankIBAN", "bankIBAN")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
          <!-- Final row -->
          <fo:table-row>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.visitingCountry", "The Netherlands")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                  ${_("reproductionInvoice.postalCountry", "The Netherlands")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.website", "iisg.amsterdam")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
                ${_("reproductionInvoice.bankSWIFT", "swift")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block>
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
        </fo:table-body>
      </fo:table>
    </fo:static-content>

    <fo:flow flow-name="xsl-region-body" font-size="0.6em">
      <#nested>
    </fo:flow>
  </fo:page-sequence>
</fo:root>
</#macro>
