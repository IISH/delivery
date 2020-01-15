<#include "base.ftl"/>
<#include "form.ftl" />

<#-- Build the title -->
<#assign title>
  <#if original??>
    <@_ "reproductionEdit.title" "Edit Reproduction"/> ${original.id?c}
  <#else>
    <@_ "reproductionMassCreate.title" "New Reproduction"/>
  </#if>
</#assign>

<#-- Build the page -->
<@preamble title/>
<@heading/>
<@body>
<h1>${title}</h1>
<fieldset class="reproduction_form">
<@form "" "reproduction">
  <ul class="form">
    <li><@input "reproduction.customerName" ""/></li>
    <li><@input "reproduction.customerEmail" ""/></li>
    <li><@input "reproduction.comment" "" false/></li>
  </ul>

  <ul id="holdingReproductions" class="holdingReproductionDetails actions">
    <li id="newHoldingReproduction" class="hidden">
      <input type="button" class="removeButton"
             onclick="removeNewHoldingReproduction($(this).parent());renumberHoldingReproductions();"
             value="<@_ "deleteHolding.submit"/>"/>
      <input type="button" class="addButton" onclick="addNewHoldingReproduction($(this) .parent());"
             value="<@_ "addHolding.submit"/>"/>
    </li>

    <#assign i = 0>
    <#list reproduction.holdingReproductions as hr>
      <#assign record = hr.holding.record>
      <#assign info = record.externalInfo>

      <#assign reproductionOptions = []/>
      <#if reproductionStandardOptions?? && reproductionStandardOptions[hr.holding.signature]??>
        <#assign reproductionOptions = reproductionStandardOptions[hr.holding.signature]/>
      </#if>

      <li class="holdingReproductions" id="holdingReproduction${i}">
        <#if original??>
          &bull;
        <#else>
          <input type="button" class="removeButton"
                 onclick="removeNewHoldingReproduction($(this).parent());renumberHoldingReproductions();"
                 value="<@_ "deleteHolding.submit"/>"/>
        </#if>

        <input type="hidden" id="holdingReproductions[${i}].holding" name="holdingReproductions[${i}].holding"
               class="holding" value="${hr.holding.id?c}"/>

        <ul class="inner">
          <li>
            ${record.title?html} <#if info.author??>/
            ${info.author?html}</#if> - ${hr.holding.signature?html}
          </li>

          <#if !original?? || (original?? && hr.standardOption??)>
            <li>
              <#if original??>
                <label class="group">
                  <input type="hidden" name="holdingReproductions[${i}].standardOption" value="${hr.standardOption.id?c}"/>
                  ${hr.standardOption.optionName?html}
                </label>

                <span>
                  (&euro; ${hr.completePrice?string("0.00")} - ${hr.deliveryTime?html} <@_ "days" "days"/>)
                </span>
              <#else>
                <#list reproductionOptions as value>
                  <label class="group">
                    <input type="radio" class="standardOption" name="holdingReproductions[${i}].standardOption"
                           value="${value.id?c}"<#if hr.standardOption?? && hr.standardOption.id == value.id> checked="checked"</#if>/>
                    ${value.optionName?html}
                  </label>
                    <span>
                      (&euro; ${record.determinePrice(value.price)?string("0.00")} - ${value.deliveryTime?html} <@_ "days" "days"/>)
                    </span>
                </#list>

                <#if (hr.id lt 0) || (hr.holding.status == "AVAILABLE")>
                  <label class="group">
                    <input type="radio" name="holdingReproductions[${i}].standardOption"
                           class="standardOption custom" value="0" <#if !hr.standardOption??> checked="checked"</#if>/>
                    <@_ "reproduction.customReproduction.backend" "Custom reproduction"/>
                  </label>
                </#if>
              </#if>
            </li>
          </#if>

          <#if info.materialType == "SERIAL">
            <li>
              <label class="group">
                <strong class="label"><@_ "holdingReproductions.comment" "" /></strong>
                <@input_nolabel "reproduction.holdingReproductions[${i}].comment" "comment" />

                <#if hr.holding.externalInfo.serialNumbers??>
                  ( <@_ "reproductionCreate.serialAvailable" ""/>:
                ${hr.holding.externalInfo.serialNumbers} )
                </#if>
              </label>
            </li>
          </#if>

          <li class="on-custom" <#if hr.standardOption??> style="display:none;"</#if>>
            <label class="group">
              <strong class="label"><@_ "reproductionStandardOption.price" "Price"/></strong>
              <@input_nolabel "reproduction.holdingReproductions[${i}].price" "price" "EUR" />
            </label>
          </li>

          <li class="on-custom" <#if hr.standardOption??> style="display:none;"</#if>>
            <label class="group">
              <strong class="label"><@_ "reproductionStandardOption.deliveryTime" "Estimated delivery time"/></strong>
              <@input_nolabel "reproduction.holdingReproductions[${i}].deliveryTime" "deliveryTime" "days" />
            </label>
          </li>

          <li class="on-custom" <#if hr.standardOption??> style="display:none;"</#if>>
            <label class="group">
              <strong class="label"><@_ "reproduction.customReproductionCustomer.backend" "Customer's wish"/></strong>
              <@textarea_nolabel "reproduction.holdingReproductions[${i}].customReproductionCustomer" "customReproductionCustomer" />
            </label>
          </li>

          <li class="on-custom" <#if hr.standardOption??> style="display:none;"</#if>>
            <label class="group">
              <strong class="label"><@_ "reproduction.customReproductionReply" "Reply on wish"/></strong>
              <@textarea_nolabel "reproduction.holdingReproductions[${i}].customReproductionReply" "customReproductionReply" />
            </label>
          </li>

          <#if emailResponses?? && emailResponses[hr.id?c]??>
            <li>
              <a href="mailto:${reproduction.customerEmail}?SUBJECT=<@_ "reproduction.customReproductionReplySubject" "Reply on your wish"/>&BODY=${emailResponses[hr.id?c]?url}">
                <em><@_ "reproduction.customReproductionReplyByEmail" "Reply on wish through email"/></em>
              </a>
            </li>
          </#if>
        </ul>
      </li>

      <#assign i = i + 1>
    </#list>
  </ul>

  <ul class="form">
    <li><@input "reproduction.discountPercentage" "" true "" "percentage"/></li>

    <li>
      <label for="mail" class="field"><@_ "reproductionMassCreate.mail" "Mail"/></label>
      <input type="checkbox" name="mail" id="mail" class="field" checked="checked"/>
    </li>
  </ul>
</fieldset>

<#if !original??>
  <fieldset class="actions">
    <legend><@_ "reproductionMassCreate.search" "Search for Holdings to Add"/></legend>

    <ul>
      <li>
        <label for="searchTitle"><@_ "record.title" "Title"/></label>
        <input id="searchTitle" type="text" name="searchTitle" value="${RequestParameters.searchTitle!""}"/>
      </li>
      <li>
        <label for="searchSignature"><@_ "holding.signature" "Signature"/></label>
        <input id="searchSignature" type="text" name="searchSignature" value="${RequestParameters.searchSignature!""}"/>

        <#assign searchLabel>
          <@_ "reproductionMassCreate.searchSubmit" "Search"/>
        </#assign>
      </li>
    </ul>

    <input type="submit" name="searchSubmit" value="${searchLabel}"/>

    <ul id="holdingSearch" class="holdingReproductionDetails">
      <#if holdingList??>
        <#assign results = 0>

        <#list holdingList as h>
          <#assign info = h.record.externalInfo>

          <#assign reproductionOptions = []/>
          <#if reproductionStandardOptions[h.signature]??>
            <#assign reproductionOptions = reproductionStandardOptions[h.signature]/>
          </#if>

          <#if reproductionOptions?has_content || (h.status == "AVAILABLE")>
            <#assign results = results + 1>

            <li>
              <input type="button" class="addButton" onclick="addNewHoldingReproduction($(this).parent());"
                     value="<@_ "addHolding.submit"/>"/>
              <input type="hidden" class="holding" value="${h.id?c}"/>

              <ul class="inner">
                <li>
                  ${h.record.title?html} <#if info.author??>/ ${info.author}</#if> - ${h.signature?html}
                  <#if h.record.parent??>/ <@_ "editrecord.item" "Item"/> ${h.record.pid?substring(h.record.parent.pid?length + 1)?html}</#if>
                </li>

                <li class="hidden">
                  <#list reproductionOptions as value>
                    <label class="group">
                      <input type="radio" class="standardOption" value="${value.id?c}"/>
                      ${value.optionName?html}
                    </label>
                      <span>
                        (&euro; ${h.record.determinePrice(value.price)?string("0.00")} - ${value.deliveryTime?html} <@_ "days" "days"/>)
                      </span>
                  </#list>

                  <#if h.status == "AVAILABLE">
                    <label class="group">
                      <input type="radio" class="standardOption custom" value="0" checked="checked"/>
                      <@_ "reproduction.customReproduction.backend" "Custom reproduction"/>
                    </label>
                  </#if>
                </li>

                <#if info.materialType == "SERIAL">
                  <li class="hidden">
                    <label class="group">
                      <strong class="label"><@_ "holdingReproductions.comment" "" /></strong>
                      <input type="text" class="comment"/>

                      <#if h.externalInfo.serialNumbers??>
                        ( <strong><@_ "reproductionCreate.serialAvailable" ""/>:</strong>
                      ${h.externalInfo.serialNumbers} )
                      </#if>
                    </label>
                  </li>
                </#if>

                <li class="hidden on-custom">
                  <label class="group">
                    <strong class="label"><@_ "reproductionStandardOption.price" "Price"/></strong>
                    <input type="text" class="price" value="${0}"/>
                    EUR
                  </label>
                </li>

                <li class="hidden on-custom">
                  <label class="group">
                    <strong class="label"><@_ "reproductionStandardOption.deliveryTime" "Estimated delivery time"/></strong>
                    <input type="text" class="deliveryTime" value="0"/>
                    <@_ "days" "days"/>
                  </label>
                </li>

                <li class="hidden on-custom">
                  <label class="group">
                    <strong class="label"><@_ "reproduction.customReproductionCustomer.backend" "Customer's wish"/></strong>
                    <textarea class="customReproductionCustomer"></textarea>
                  </label>
                </li>

                <li class="hidden on-custom">
                  <label class="group">
                    <strong class="label"><@_ "reproduction.customReproductionReply" "Reply on wish"/></strong>
                    <textarea class="customReproductionReply"></textarea>
                  </label>
                </li>
              </ul>
            </li>
          </#if>
        </#list>

        <#if results == 0>
          <li>
            <span><@_ "search.notfound" "No results..."/></span>
          </li>
        </#if>
      </#if>
    </ul>
  </fieldset>
</#if>

<@buttons>
  <@submit "reproduction" />
</@buttons>

</@form>
</@body>
