<#include "mail.ftl">
<@mail reservation.visitorName>
<#if reservation.date?date?string == .now?date?string>
${_("reservationMail.confirmationMessage", "With this email we confirm your reservation. Requested documents will be in the reading room within 30 minutes.")}
<#else>
${_("reservationMail.confirmationFutureMessage", "With this email we confirm your reservation. Requested documents will be in the reading room on the day of your visit as of 9.30 am.")}
</#if>

--- ${_("reservation.date", "Date")} ---
${reservation.date?string(delivery.dateFormat)}

--- ${_("reservation.records", "Records")} ---
<#list reservation.holdingReservations as hr>
* ${hr.toString()}
</#list>
</@mail>