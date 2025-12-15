<#include "mail.ftl">
<@mail reproduction.customerName>
${_("reproductionMail.payedMessage", "With this email we confirm your payment. Please see the invoice attached.")}
<#if !reproduction.isForFree()>

${_("reproductionMail.payedNotFreeMessage", "You will also receive an email from our payment provider to confirm the payment.")}
</#if>

${_("reproductionMail.reproductionId", "Reproduction number")}: ${reproduction.id?c}
<#if reproduction.order??>${_("reproductionMail.orderId", "Payment id")}: ${reproduction.orderId}</#if>
</@mail>