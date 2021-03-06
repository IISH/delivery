<#include "mail.ftl">
<@mail reproduction.customerName>
    ${_("reproductionMail.offerReminderMessage", " With this email we remind you on the payment for your reproduction request. You can complete your order on the following link:")}

${delivery.urlSelf}/reproduction/confirm/${reproduction.id?c}/${reproduction.token}?locale=${locale}

    ${_("reproductionMail.reproductionId", "Reproduction number")}: ${reproduction.id?c}

--- ${_("reproduction.records", "Items")} ---
    <#list reproduction.holdingReproductions as hr>
        <#assign h = hr.holding>
        <#assign info = h.record.externalInfo>
    * ${h.record.title} - ${h.signature} <#if info.author??>/ ${info.author} </#if><#if hr.comment??>- ${hr.comment}</#if>
        ${_("reproductionStandardOption.price", "Price")}: EUR ${hr.completePriceWithDiscount?string("0.00")}
        ${_("reproductionStandardOption.deliveryTime" "Estimated delivery time")}: ${hr.deliveryTime} ${_("days", "days")}
        <#if hr.standardOption??>
            <#if locale == 'nl'>${hr.standardOption.optionDescriptionNL}<#else>${hr.standardOption.optionDescriptionEN}</#if>

        <#else>

            ${_("reproduction.customReproductionCustomer", "Your wish")}:
        ${hr.customReproductionCustomer}

            <#if hr.customReproductionReply??>
            ${hr.customReproductionReply}

            </#if>
        </#if>
    </#list>
    <#if reproduction.getAdminstrationCosts() gt 0>
    --- ${_("including", "Including")} ---
        ${_("reproduction.adminstrationCosts", "Adminstration cost")}: ${reproduction.adminstrationCostsWithDiscount?string("0.00")} EUR

    </#if>
--- ${_("total", "Total")} ---
    ${_("reproductionStandardOption.price", "Price")}: ${reproduction.totalPriceWithDiscount?string("0.00")} EUR
    ${_("reproductionStandardOption.deliveryTime" "Estimated delivery time")}: ${reproduction.estimatedDeliveryTime} ${_("days", "days")}
</@mail>
