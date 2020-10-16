<#include "mail.ftl">
<@mailRaedingRoom>
${_("reproductionMail.pendingSubjectReadingRoomMessage", "A customer has requested a new reproduction and is waiting for an offer from the reading room.")}

${_("reproductionMail.reproductionId", "Reproduction number")}: ${reproduction.id?c}
${_("reproductionMail.customerName", "Customer")}: ${reproduction.customerName}
${_("reproductionMail.customerEmail", "E-mail")}: ${reproduction.customerEmail}
URL: ${delivery.urlSelf}/reproduction/${reproduction.id?c}/edit

--- ${_("reproduction.records", "All items")} ---
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

${_("reproduction.customReproductionCustomer.backend", "Customer's wish")}:
${hr.customReproductionCustomer}
  </#if>

</#list>
</@mailRaedingRoom>
