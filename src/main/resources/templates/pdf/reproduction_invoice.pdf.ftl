<#include "pdf.ftl">
<@pdf "reproductionMail.invoice", "Invoice">
  <!-- Header of page -->
  <fo:block font-weight="bold">
    <fo:block margin-bottom="2mm">
      ${reproduction.name?xml}
    </fo:block>

    <fo:block margin-bottom="2mm" font-weight="bold" text-align="center" font-size="3em">
      ${_("reproductionMail.invoice", "Invoice")}
    </fo:block>

    <fo:block margin-bottom="2mm" font-weight="normal">
      <fo:table table-layout="fixed" width="80%" display-align="center">
        <fo:table-column column-width="15%"/>
        <fo:table-column/>

        <fo:table-body>
          <fo:table-row>
            <fo:table-cell>
              <fo:block text-align="left">
                ${_("reproductionInvoice.invoiceNumber", "Invoice nr.")}:
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block text-align="left">
                <#if !reproduction.orderId??>
                  No order ID
                <#else>
                  ${reproduction.orderId?c}
                </#if>
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
          <fo:table-row>
            <fo:table-cell>
              <fo:block text-align="left">
                ${_("reproductionInvoice.invoiceDate", "Invoice date")}:
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block text-align="left">
                <#if reproduction.datePaymentAccepted??>
                  ${reproduction.datePaymentAccepted?string("dd-MM-yyyy")}
                </#if>
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
          <fo:table-row>
            <fo:table-cell>
              <fo:block text-align="left">
                ${_("reproductionInvoice.reference", "Your reference")}:
              </fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block text-align="left">
                ${reproduction.id?c}
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
        </fo:table-body>
      </fo:table>
    </fo:block>
  </fo:block>

  <!-- summary of items -->
  <fo:block margin-top="1cm">
    <fo:table table-layout="fixed" width="100%" display-align="center">
      <fo:table-column/>
      <fo:table-column column-width="1.6cm"/>
      <fo:table-column column-width="1.6cm"/>
      <fo:table-column column-width="1.6cm"/>
      <fo:table-column column-width="2cm"/>
      <fo:table-column column-width="3.2cm"/>

      <fo:table-header font-weight="bold" border-bottom="0.5pt solid black">
        <fo:table-cell padding="1mm">
          <fo:block>
            ${_("reproduction.confirm.item", "Item")}
          </fo:block>
        </fo:table-cell>
        <fo:table-cell padding="1mm">
          <fo:block>
            ${_("reproduction.confirm.price", "Price")}
          </fo:block>
        </fo:table-cell>
        <fo:table-cell padding="1mm">
          <fo:block>
            ${_("reproduction.confirm.discount", "Discount")}
          </fo:block>
        </fo:table-cell>
        <fo:table-cell padding="1mm">
          <fo:block>
            ${_("reproduction.confirm.price.total", "Total price")}
          </fo:block>
        </fo:table-cell>
        <fo:table-cell padding="1mm">
          <fo:block>
            ${_("reproduction.confirm.btw", "BTW included")}
          </fo:block>
        </fo:table-cell>
        <fo:table-cell padding="1mm">
          <fo:block>
            ${_("reproduction.confirm.delivery.time", "Estimated delivery time")}
          </fo:block>
        </fo:table-cell>
      </fo:table-header>

      <fo:table-footer font-weight="bold" border-top="0.5pt solid black">
        <#assign btwPrices = reproduction.totalBTW/>
        <#list btwPrices?keys as btwPercentage>
          <#if btwPercentage?is_first>
            <fo:table-row>
              <fo:table-cell padding="1mm" number-rows-spanned="${btwPrices?size}">
                <fo:block text-align="right">
                  ${_("total", "Total")}:
                </fo:block>
              </fo:table-cell>
              <fo:table-cell padding="1mm" number-rows-spanned="${btwPrices?size}">
                <fo:block>
                  &#8364; ${reproduction.totalPrice?string("0.00")}
                </fo:block>
              </fo:table-cell>
              <fo:table-cell padding="1mm" number-rows-spanned="${btwPrices?size}">
                <fo:block>
                  &#8364; -${reproduction.totalDiscount?string("0.00")}
                </fo:block>
              </fo:table-cell>
              <fo:table-cell padding="1mm" number-rows-spanned="${btwPrices?size}">
                <fo:block>
                  &#8364; ${reproduction.totalPriceWithDiscount?string("0.00")}
                </fo:block>
              </fo:table-cell>
              <fo:table-cell padding="1mm">
                <fo:block>
                  &#8364; ${btwPrices[btwPercentage]?string("0.00")} (${btwPercentage}&#37;)
                </fo:block>
              </fo:table-cell>
              <fo:table-cell padding="1mm" number-rows-spanned="${btwPrices?size}">
                <fo:block>
                  ${reproduction.getEstimatedDeliveryTime()?xml} ${_("days", "days")}
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
          <#else>
            <fo:table-row>
              <fo:table-cell padding="1mm">
                <fo:block>
                  &#8364; ${btwPrices[btwPercentage]?string("0.00")} (${btwPercentage}&#37;)
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
          </#if>
        </#list>
      </fo:table-footer>

      <fo:table-body>
        <#list reproduction.holdingReproductions as hr>
          <#assign h = hr.holding/>

          <fo:table-row>
            <fo:table-cell padding="1mm">
              <fo:block>
                ${h.record.externalInfo.title?xml} - ${h.signature?xml}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell padding="1mm">
              <fo:block>
                &#8364; ${hr.completePrice?string("0.00")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell padding="1mm">
              <fo:block>
                &#8364; -${hr.discount?string("0.00")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell padding="1mm">
              <fo:block>
                &#8364; ${hr.completePriceWithDiscount?string("0.00")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell padding="1mm">
              <fo:block>
                &#8364; ${hr.btwPrice?string("0.00")} (${hr.btwPercentage}&#37;)
              </fo:block>
            </fo:table-cell>
            <fo:table-cell padding="1mm">
              <fo:block>
                ${hr.deliveryTime?xml} ${_("days", "days")}
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
        </#list>

        <#if reproduction.getAdminstrationCosts() gt 0>
          <fo:table-row font-style="italic">
            <fo:table-cell padding="1mm">
              <fo:block text-align="right">
                ${_("reproduction.adminstrationCosts", "Adminstration costs")}:
              </fo:block>
            </fo:table-cell>
            <fo:table-cell padding="1mm">
              <fo:block>
                &#8364; ${reproduction.adminstrationCosts?string("0.00")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell padding="1mm">
              <fo:block>
                &#8364; -${reproduction.adminstrationCostsDiscount?string("0.00")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell padding="1mm">
              <fo:block>
                &#8364; ${reproduction.adminstrationCostsWithDiscount?string("0.00")}
              </fo:block>
            </fo:table-cell>
            <fo:table-cell padding="1mm">
              <fo:block>
                &#8364; ${reproduction.adminstrationCostsBtwPrice?string("0.00")}
                (${reproduction.adminstrationCostsBtwPercentage}&#37;)
              </fo:block>
            </fo:table-cell>
            <fo:table-cell padding="1mm">
              <fo:block/>
            </fo:table-cell>
          </fo:table-row>
        </#if>
      </fo:table-body>
    </fo:table>
  </fo:block>

  <!-- Overview of prices -->
  <fo:block margin-top="10mm" width="100%">
    <fo:table table-layout="fixed" width="100%" display-align="center">
      <fo:table-column column-width="40%"/>
      <fo:table-column column-width="60%"/>

      <fo:table-body>
        <!-- Total VAT -->
        <fo:table-row>
          <fo:table-cell>
            <fo:block-container width="100%">
              <fo:table width="100%" table-layout="fixed">
                <!-- Taxes columns -->
                <fo:table-column column-width="35%"/>
                <fo:table-column column-width="35%"/>
                <fo:table-column column-width="30%"/>

                <fo:table-header font-weight="bold" border-top="0.5pt solid black">
                  <fo:table-cell>
                    <fo:block>
                      ${_("reproductionInvoice.tax", "VAT")}
                    </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block>
                      ${_("reproductionInvoice.base", "Base")}
                    </fo:block>
                  </fo:table-cell>
                  <fo:table-cell>
                    <fo:block>
                      ${_("reproductionInvoice.taxAmount", "VAT amount")}
                    </fo:block>
                  </fo:table-cell>
                </fo:table-header>

                <fo:table-body>
                  <#list reproduction.holdingReproductions as hr>
                    <fo:table-row>
                      <fo:table-cell>
                        <fo:block>
                          &#8364; ${hr.btwPercentage}&#37;
                        </fo:block>
                      </fo:table-cell>
                      <fo:table-cell>
                        <fo:block>
                          &#8364; ${hr.getCompletePriceWithoutTax()?string("0.00")}
                        </fo:block>
                      </fo:table-cell>
                      <fo:table-cell>
                        <fo:block>
                          &#8364; ${hr.btwPrice?string("0.00")}
                        </fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                  </#list>

                  <#if reproduction.getAdminstrationCosts() gt 0>
                    <fo:table-row>
                      <fo:table-cell>
                        <fo:block>
                          &#8364; ${reproduction.adminstrationCostsBtwPercentage}&#37;
                        </fo:block>
                      </fo:table-cell>
                      <fo:table-cell>
                        <fo:block>
                          &#8364; ${reproduction.getAdminstrationCostsWithoutTax()?string("0.00")}
                        </fo:block>
                      </fo:table-cell>
                      <fo:table-cell>
                        <fo:block>
                          &#8364; ${reproduction.adminstrationCostsBtwPrice?string("0.00")}
                        </fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                  </#if>
                </fo:table-body>
              </fo:table>
            </fo:block-container>
          </fo:table-cell>

          <!-- Total costs -->
          <fo:table-cell>
            <fo:block-container width="100%">
              <fo:table width="100%" table-layout="fixed">
                <!-- Pricing columns -->
                <fo:table-column column-width="60%"/>
                <fo:table-column column-width="20%"/>
                <fo:table-column column-width="20%"/>

                <fo:table-body>
                  <fo:table-row>
                    <fo:table-cell>
                      <fo:block>
                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                      <fo:block>
                        ${_("reproductionInvoice.totalExVAT", "Total ex. VAT")}:
                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell text-align="right">
                      <fo:block>
                        &#8364; ${reproduction.getTotalPriceExclBTW()?string("0.00")}
                      </fo:block>
                    </fo:table-cell>
                  </fo:table-row>
                  <fo:table-row>
                    <fo:table-cell>
                      <fo:block>
                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                      <fo:block>
                        ${_("reproductionInvoice.totalVAT", "Total VAT")}:
                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell text-align="right">
                      <fo:block>
                        &#8364; ${reproduction.getTotalBTWPrice()?string("0.00")}
                      </fo:block>
                    </fo:table-cell>
                  </fo:table-row>
                  <fo:table-row>
                    <fo:table-cell>
                      <fo:block>
                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell border-bottom="0.5px solid black">
                      <fo:block>
                        ${_("reproduction.confirm.discount", "Discount")}:
                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell border-bottom="0.5px solid black">
                      <fo:block text-align="right">
                        &#8364; -${reproduction.totalDiscount?string("0.00")}
                      </fo:block>
                    </fo:table-cell>
                  </fo:table-row>
                  <fo:table-row>
                    <fo:table-cell>
                      <fo:block>
                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell>
                      <fo:block>
                        ${_("reproductionInvoice.totalIncVAT", "Total VAT Inc.")}:
                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell text-align="right">
                      <fo:block>
                        &#8364; ${reproduction.totalPriceWithDiscount?string("0.00")}
                      </fo:block>
                    </fo:table-cell>
                  </fo:table-row>
                </fo:table-body>
              </fo:table>
            </fo:block-container>
          </fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
  </fo:block>
</@pdf>
