<#import "spring.ftl" as spring/>
<#include "base.ftl">

<#assign title=_("reservation.success", "Reservation placed successfully.")/>

<@userbase title true>
  <section>
    <heading>
      <h1>${title}</h1>
    </heading>

    <section>
      <ul class="items">
        <#list reservation.holdingReservations as hr>
          <#assign h = hr.holding>
          <#assign info = h.record.externalInfo>

          <li>${hr.toString()}</li>
        </#list>
      </ul>
    </section>

    <#if reservation.date?date?string == .now?date?string>
      <p>${_("reservation.successMsg", "Requested documents will be in the reading room within <strong>30 minutes</strong>.")}</p>
    <#else>
      <p>${_("reservation.successFutureMsg", "Requested documents will be in the reading room on the day of your visit as of <strong>9.30 am</strong>.")}</p>
    </#if>

    <#if error?? >
      <p class="error">${_("reservation.error."+error, error)}</p>
    </#if>

    <p>${_("reservation.backToSearch", "Close this tab and return to Search for new requests")}</p>
  </section>
</@userbase>
