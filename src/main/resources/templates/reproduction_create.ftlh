<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=_("reproduction.create", "Create Reproduction")/>
<#assign email=_("iisg.email", "")/>

<@preamble title />
<@userHeading />
<@body>
  <h1>${title}</h1>

  <ol class="progress on-any-non-custom" style="display:none;">
    <li class="step active">
      1. ${_("reproduction.steps.request", "Request reproduction")}
    </li>
    <li class="step">
      2. ${_("reproduction.steps.confirm", "Confirm reproduction request")}
    </li>
    <li class="step">
      3. ${_("reproduction.steps.payment", "Payment of reproduction")}
    </li>
    <li class="step">
      4. ${_("reproduction.steps.delivery", "Delivery of reproduction")}
    </li>
  </ol>

  <ol class="progress on-all-custom">
    <li class="step active">
      1. ${_("reproduction.steps.request", "Request reproduction")}
    </li>
    <li class="step">
      2. ${_("reproduction.steps.wait", "Wait for offer")}
    </li>
    <li class="step">
      3. ${_("reproduction.steps.confirm", "Confirm reproduction request")}
    </li>
    <li class="step">
      4. ${_("reproduction.steps.payment", "Payment of reproduction")}
    </li>
    <li class="step">
      5. ${_("reproduction.steps.delivery", "Delivery of reproduction")}
    </li>
  </ol>

  <section>
    <@form "" "reproduction" "create">
      <heading>
        <hgroup>
          <fieldset class="reproductionItemsSelect">
            <legend>${_("reproduction.records", "Items")}</legend>

              <#assign idx = 0>
              <#list reproduction.holdingReproductions as hr>
                <#assign h = hr.holding>
                <#assign info = h.record.externalInfo>

                <input name="holdingReproductions[${idx}].holding" type="hidden" value="${h.id?c}"/>

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
                      <@input_nolabel "reproduction.holdingReproductions[${idx}].comment" />

                      <#if h.externalInfo.serialNumbers??>
                        ( <strong>${_("reproductionCreate.serialAvailable", "")}
                        :</strong> ${h.externalInfo.serialNumbers} )
                      </#if>
                    </li>
                  </#if>
                </ul>

                <#assign reproductionOptions = []/>
                <#if reproductionStandardOptions[h.signature]??>
                  <#assign reproductionOptions = reproductionStandardOptions[h.signature]/>
                </#if>

                <#assign unavailable = []/>
                <#if unavailableStandardOptions[h.signature]??>
                  <#assign unavailable = unavailableStandardOptions[h.signature]/>
                </#if>

                <@reproductionHoldingOptions "reproduction.holdingReproductions[${idx}].standardOption" reproductionOptions unavailable h/>

                <#assign idx = idx + 1>
              </#list>
          </fieldset>
        </hgroup>
      </heading>

      <div class="reproduction_form">
        <fieldset>
          <@input "reproduction.customerName" ""/>
          <@input "reproduction.customerEmail" ""/>

          <label for="captcha_response_field" class="field">
            ${_("captcha.explanation", "Type the following word to prevent spam")}
            <span class="red">*</span>
          </label>

          <div id="captcha_widget_div" class="field">
            <input type="text" id="captcha_response_field" name="captcha_response_field" value="" class="field"
                   autocomplete="off"/>
            <img src="<@spring.url relativeUrl="/captcha"/>" id="captcha_image"/>
            <a href="#" class="refreshCaptcha">
              ${_("captcha.refresh", "Refresh captcha")}
            </a>
          </div>

          <#if captchaError?? >
            <ul class="errors">
              <li>
                <b>${captchaError}</b>
              </li>
            </ul>
          </#if>
        </fieldset>

        <@buttons>
            <@submit "reproduction" />
        </@buttons>
      </div>
    </@form>
  </section>
</@body>

<#macro reproductionHoldingOptions path options unavailable holding>
  <@spring.bind path/>

  <ul class="holdingDetails">
    <#list options as value>
      <#assign available = true/>
      <#list unavailable as unavailableStandardOption>
        <#if value.id == unavailableStandardOption.id>
          <#assign available = false/>
        </#if>
      </#list>

      <li>
        <#assign id="${spring.status.expression}.${value_index}">
        <input type="radio" id="${id}" name="${spring.status.expression}"
               value="${value.id?c}"
                <#if !available> disabled="disabled"</#if>
                <#if spring.stringStatusValue == value> checked="checked"</#if>/>

        <ul class="reproductionDetails create">
          <li>
            <#assign label=path + "." + value.optionName />
            <label for="${id}">${_(label value.optionName)}</label>
          </li>

          <li>
            <em>${value.optionDescription}</em>
          </li>

          <#if !available>
            <li class="warning spacing">
              ${_("reproduction.reservedNotInSorMsg", "Unfortunately, you cannot select this option. This item is already reserved and not yet digitally available.")}
              <br/>
              ${_("reproduction.reservedMoreInfoMsg", "For more information, please contact the information desk:")}
              <a href="mailto:${email}">${email}</a>
            </li>
          </#if>

          <li class="spacing">
            <span>${_("reproductionStandardOption.price", "Price")}</span>
            <@holdingPrice value.price holding.record.determinePrice(value.price) holding.record.externalInfo.materialType holding.record.pages.numberOfPages/>
          </li>

          <li>
            <span>${_("reproductionStandardOption.deliveryTime", "Estimated delivery time")}</span>
            ${value.deliveryTime} ${_("days", "days")}
          </li>
        </ul>
      </li>
    </#list>

    <li>
      <#assign id="${spring.status.expression}.null">
      <#assign materialType = holding.record.externalInfo.materialType>
      <input type="radio" id="${id}" name="${spring.status.expression}" class="custom" value="0"
              <#if holding.status != "AVAILABLE"> disabled="disabled"</#if>
              <#if (holding.status == "AVAILABLE") && spring.stringStatusValue == ""> checked="checked"</#if>/>

      <ul class="reproductionDetails">
        <li>
          <label for="${id}">${_("reproduction.customReproduction", "Request offer")}</label>
        </li>

        <li>
          <em>
            ${_("reproduction.customReproductionCustomer.description", "Describe your wishes as exactly as possible. You will get an offer by email.")}
            <#if reproductionCustomNotes[materialType]?? && reproductionCustomNotes[materialType].note??>
              <br/> ${reproductionCustomNotes[materialType].note}
            </#if>
          </em>
        </li>

        <#if holding.status != "AVAILABLE">
          <li class="warning spacing">
            ${_("reproduction.reservedNotInSorMsg", "Unfortunately, you cannot select this option. This item is already reserved and not yet digitally available.")}
            <br/>
            ${_("reproduction.reservedMoreInfoMsg", "For more information, please contact the information desk:")}
            <a href="mailto:${email}">${email}</a>
          </li>
        </#if>

        <li class="spacing">
          <span>${_("reproductionStandardOption.price", "Price")}</span>
          ${_("tbd", "To be determined")}
        </li>

        <li>
          <span>${_("reproductionStandardOption.deliveryTime", "Estimated delivery time")}</span>
          ${_("tbd", "To be determined")}
        </li>

        <#if holding.status == "AVAILABLE">
          <li class="spacing">
            <@textarea_nolabel "reproduction.holdingReproductions[${idx}].customReproductionCustomer"/>
          </li>
        </#if>
      </ul>
    </li>
  </ul>
</#macro>
