<#include "mail.ftl">
<@mailRaedingRoom>
${_("reproductionMail.activeReproductionMessage", "A reproduction has been confirmed and paid.")}


${_("reproductionMail.reproductionId", "Reproduction number")}: ${reproduction.id?c}
<#if reproduction.order??>${_("reproductionMail.orderId", "Payment id")}: ${reproduction.order.id}</#if>
${_("reproductionMail.customerName", "Customer")}: ${reproduction.customerName}
${_("reproductionMail.customerEmail", "E-mail")}: ${reproduction.customerEmail}

<#if notInSor?has_content>
${_("reproductionMail.activeReproductionNotInSorMessage", "The following items have been sent to the printer for repro:")}


<#list notInSor as hr>
* ${hr.toString()}

</#list>
</#if>

<#if inSor?has_content>
${_("reproductionMail.activeReproductionInSorMessage", "The following items can be found in the SOR:")}

${_("reproductionMail.sorLinksWarningMessage", "WARNING: These links should not be emailed to the customer!!! Please use FileSender / WeTransfer to sent all reproductions to the customer!!!")}


<#list inSor as hr>
* ${hr.toString()}
    <#list sorUrls[hr.holding.signature] as url>
${url}
    </#list>
</#list>
</#if>
</@mailRaedingRoom>
