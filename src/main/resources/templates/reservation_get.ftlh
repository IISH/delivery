<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=_("reservationSingle.title", "Reservation") + " " + reservation.id?c/>

<@base "${title}">
  <h1>${title}</h1>

  <ul class="reservationDetails">
    <li><span>${_("reservation.visitorName", "Name")}</span> ${reservation.visitorName}</li>
    <li><span>${_("reservation.visitorEmail", "E-mail")}</span> ${reservation.visitorEmail}</li>
    <li><span>${_("reservation.date", "Date")}</span> ${reservation.date?string(delivery.dateFormat)}</li>

    <#if reservation.returnDate??>
      <li>
        <span>${_("reservation.returnDate", "Return Date")}</span>${reservation.returnDate?string(delivery.dateFormat)}
      </li>
    </#if>

    <li>
      <span>${_("reservation.status", "Status")}</span> ${_("reservation.statusType.${reservation.status}" reservation.status?string)}
    </li>

    <#if reservation.comment??>
      <li><span>${_("reservation.comment", "Comment")}</span>${reservation.comment}</li>
    </#if>

    <#if _sec.ifAllGranted("ROLE_RESERVATION_CREATE")>
      <li><br/><a href="${rc.contextPath}/reservation/masscreateform?fromReservationId=${reservation.id?c}">
              ${_("reservationSingle.newReservation")}</a></li>
    </#if>

    <#if _sec.ifAllGranted("ROLE_REPRODUCTION_CREATE,ROLE_RESERVATION_DELETE")>
      <li>
        <a href="${rc.contextPath}/reservation/${reservation.id?c}/convert">
          ${_("reservationSingle.convert")}
        </a>
      </li>
    </#if>

    <table class="records">
      <caption>${_("reservation.holdings", "Holdings")}</caption>

      <thead>
        <tr>
          <th>ID</th>
          <th>${_("record.title", "Title")}</th>
          <th>PID</th>
          <th>${_("reservation.printed", "Printed")}</th>
          <th>${_("holding.status", "Status")}</th>
        </tr>
      </thead>

      <tbody>
        <#list reservation.holdingReservations as hr>
          <#assign h = hr.holding>

          <tr>
            <td>${h.id?c}</td>
            <td>${hr.toShortString()}</td>
            <td>
              <#if _sec.ifAllGranted("ROLE_RECORD_MODIFY")>
                <a target="_blank" href="${rc.contextPath}/record/editform/${h.record.pid?url}">${h.record.pid}</a>
              <#else>
                ${h.record.pid}
              </#if>
            </td>
            <td>${hr.printed?string(_("yes", "Yes"), _("no", "No"))}</td>
            <td><@holdingStatus holdingActiveRequests reservation h/></td>
          </tr>
        </#list>
      </tbody>
    </table>
  </ul>
</@base>
