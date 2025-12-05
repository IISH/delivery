<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=_("reproduction.confirm", "Confirm reproduction")/>

<@preamble title />
<@userHeading />
<@body>
  <h1>${title}</h1>

  <#if reproduction.isOfferReadyImmediatly()>
    <ol class="progress on-all-non-custom">
      <li class="step">
        1. ${_("reproduction.steps.request", "Request reproduction")}
      </li>
      <li class="step active">
        2. ${_("reproduction.steps.confirm", "Confirm reproduction request")}
      </li>
      <li class="step">
        3. ${_("reproduction.steps.payment", "Payment of reproduction")}
      </li>
      <li class="step">
        4. ${_("reproduction.steps.delivery", "Delivery of reproduction")}
      </li>
    </ol>
  <#else>
    <ol class="progress on-any-custom">
      <li class="step">
        1. ${_("reproduction.steps.request", "Request reproduction")}
      </li>
      <li class="step">
        2. ${_("reproduction.steps.wait", "Wait for offer")}
      </li>
      <li class="step active">
        3. ${_("reproduction.steps.confirm", "Confirm reproduction request")}
      </li>
      <li class="step">
        4. ${_("reproduction.steps.payment", "Payment of reproduction")}
      </li>
      <li class="step">
        5. ${_("reproduction.steps.delivery", "Delivery of reproduction")}
      </li>
    </ol>
  </#if>

  <#if paymentError??>
    <div class="errors">${paymentError}</div>
  </#if>

  <#if error??>
    <div class="errors">${_("reproduction.error."+error, error)}</div>
  </#if>

  <section>
    <@form "" "reproduction" "confirm">
      <heading>
        <hgroup>
          <fieldset>
            <legend>
              ${_("reproduction.records", "")}
            </legend>

            <#list reproduction.holdingReproductions as hr>
              <#assign h = hr.holding>
              <#assign info = h.record.externalInfo>

              <h3>${info.title}</h3>

              <ul class="holdingDetails">
                <li>
                  <span>${_("record.externalInfo.materialType", "Material Type")}</span>
                  ${_("record.externalInfo.materialType.${info.materialType}", "")}
                </li>

                <li>
                  <span>${_("holding.signature", "Signature")}</span>
                  ${h.signature}
                </li>

                <#if info.author??>
                  <li>
                    <span>${_("record.externalInfo.author", "Author")}</span>
                    ${info.author}
                  </li>
                </#if>

                <#if info.displayYear??>
                  <li>
                    <span>${_("record.externalInfo.displayYear", "Year")}</span>
                    ${info.displayYear}
                  </li>
                </#if>

                <#if info.materialType == "SERIAL">
                  <li>
                    <span>${_("holdingReproductions.comment", "Comment")} </span>
                    ${hr.comment}
                  </li>
                </#if>
              </ul>

              <ul class="reproductionDetails create">
                <#if hr.standardOption??>
                  <li>
                    <span>${hr.standardOption.optionName}</span>
                  </li>

                  <li>
                    <em>${hr.standardOption.optionDescription}</em>
                  </li>
                <#else>
                  <li>
                    <span>${_("reproduction.customReproduction", "Request offer")}</span>
                  </li>

                  <li>
                    <em>${hr.customReproductionCustomer}</em>
                  </li>

                  <#if hr.customReproductionReply??>
                    <li class="spacing">
                      <em>${hr.customReproductionReply}</em>
                    </li>
                  </#if>
                </#if>

                <li class="spacing">
                  <span>${_("reproductionStandardOption.price", "Price")}</span>
                  <@holdingPrice hr.price hr.completePrice info.materialType hr.numberOfPages/>
                </li>

                <li>
                  <span>${_("reproductionStandardOption.deliveryTime", "Estimated delivery time")}</span>
                  ${hr.deliveryTime} ${_("days", "days")}
                </li>
              </ul>
            </#list>
          </fieldset>
        </hgroup>
      </heading>

      <div class="reproduction_confirm_form">
        <fieldset>
          <table>
            <thead>
            <tr>
              <td>${_("reproduction.confirm.item", "Item")}</td>
              <td class="price_column">${_("reproduction.confirm.price", "Price")}</td>
              <td class="price_column">${_("reproduction.confirm.discount", "Discount")}</td>
              <td class="price_column">${_("reproduction.confirm.price.total", "Total price")}</td>
              <td class="price_column">${_("reproduction.confirm.btw", "BTW included")}</td>
              <td>${_("reproduction.confirm.delivery.time", "Estimated delivery time")}</td>
            </tr>
            </thead>

            <tbody>
            <#list reproduction.holdingReproductions as hr>
              <#assign h = hr.holding/>
              <tr>
                <td>${h.record.externalInfo.title} - ${h.signature}</td>
                <td class="price_column">&euro; ${hr.completePrice?string("0.00")}</td>
                <td class="price_column">&euro; -${hr.discount?string("0.00")}</td>
                <td class="price_column">&euro; ${hr.completePriceWithDiscount?string("0.00")}</td>
                <td class="price_column">&euro; ${hr.btwPrice?string("0.00")} (${hr.btwPercentage}&percnt;)</td>
                <td>${hr.deliveryTime} ${_("days", "days")}</td>
              </tr>
            </#list>

            <#if reproduction.getAdminstrationCosts() gt 0>
              <tr class="additional">
                <td class="label">${_("reproduction.adminstrationCosts", "Adminstration costs")}:</td>
                <td class="price_column">&euro; ${reproduction.adminstrationCosts?string("0.00")}</td>
                <td class="price_column">&euro; -${reproduction.adminstrationCostsDiscount?string("0.00")}</td>
                <td class="price_column">&euro; ${reproduction.adminstrationCostsWithDiscount?string("0.00")}</td>
                <td class="price_column">&euro; ${reproduction.adminstrationCostsBtwPrice?string("0.00")}
                  (${reproduction.adminstrationCostsBtwPercentage}&percnt;)
                </td>
                <td>&nbsp;</td>
              </tr>
            </#if>

            <#assign btwPrices = reproduction.totalBTW/>
            <#list btwPrices?keys as btwPercentage>
              <#if btwPercentage?is_first>
                <tr class="total first">
                  <td class="label" rowspan="${btwPrices?size}">${_("total", "Total")}:</td>
                  <td class="price_column" rowspan="${btwPrices?size}">
                    &euro; ${reproduction.totalPrice?string("0.00")}
                  </td>
                  <td class="price_column" rowspan="${btwPrices?size}">
                    &euro; -${reproduction.totalDiscount?string("0.00")}
                  </td>
                  <td class="price_column" rowspan="${btwPrices?size}">
                    &euro; ${reproduction.totalPriceWithDiscount?string("0.00")}
                  </td>
                  <td class="price_column">&euro; ${btwPrices[btwPercentage]?string("0.00")}
                    (${btwPercentage}&percnt;)
                  </td>
                  <td rowspan="${btwPrices?size}">
                    ${reproduction.getEstimatedDeliveryTime()} ${_("days", "days")}
                  </td>
                </tr>
              <#else>
                <tr class="total">
                  <td class="price_column">&euro; ${btwPrices[btwPercentage]?string("0.00")}
                    (${btwPercentage}&percnt;)
                  </td>
                </tr>
              </#if>
            </#list>
            </tbody>
          </table>

          <div class="accept">
            <p>${_("terms.text")}</p>

            <label>
              <input type="checkbox" id="accept_terms_conditions" name="accept_terms_conditions" value="accept"/>
              ${_("terms.accept", "I accept the terms and conditions")}
            </label>

            <#if acceptError??>
              <span class="errors">${acceptError}</span>
            </#if>
          </div>
        </fieldset>

        <@buttons>
          <@submit "reproduction"/>
        </@buttons>
      </div>
    </@form>
  </section>
</@body>
