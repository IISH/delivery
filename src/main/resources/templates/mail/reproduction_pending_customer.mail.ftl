<#include "mail.ftl">
<@mail reproduction.customerName>
${_("reproduction.requestSuccess", "Your reproduction request has been successfully received. You will soon receive an offer from us.")}

${_("reproductionMail.reproductionId", "Reproduction number")}: ${reproduction.id?c}

--- ${_("reproduction.records", "Items")} ---
<#list reproduction.holdingReproductions as hr>
  <#assign h = hr.holding>
  <#assign info = h.record.externalInfo>
* ${hr.toString()}
  <#if hr.standardOption??>
${_("reproductionStandardOption.price", "Price")}: <@holdingPrice hr.price hr.completePrice info.materialType hr.numberOfPages/>
${_("reproductionStandardOption.deliveryTime" "Estimated delivery time")}: ${hr.deliveryTime} ${_("days", "days")}
<#if locale == 'nl'>${hr.standardOption.optionDescriptionNL}<#else>${hr.standardOption.optionDescriptionEN}</#if>
  <#else>
${_("reproductionStandardOption.price" "Price")}: ${_("tbd", "To be determined")}
${_("reproductionStandardOption.deliveryTime" "Estimated delivery time")}: ${_("tbd", "To be determined")}

${_("reproduction.customReproductionCustomer", "Your wish")}:
${hr.customReproductionCustomer}
  </#if>

</#list>
</@mail>
