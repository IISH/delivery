<#include "mail.ftl">
<@mailRaedingRoom>
<@_ "reproductionMail.cancelledReproductionMessage" "A reproduction has been cancelled." />


<@_ "reproductionMail.reproductionId" "Reproduction number" />: ${reproduction.id?c}
<@_ "reproductionMail.customerName" "Customer" />: ${reproduction.customerName}
<@_ "reproductionMail.customerEmail" "E-mail" />: ${reproduction.customerEmail}

--- <@_ "reproduction.records" "All items"/> ---
<#list reproduction.holdingReproductions as hr>
    <#assign h = hr.holding>
    <#assign info = h.record.externalInfo>
* ${h.record.title} - ${h.signature} <#if info.author??>/ ${info.author} </#if><#if hr.comment??>- ${hr.comment}</#if>

</#list>
</@mailRaedingRoom>
