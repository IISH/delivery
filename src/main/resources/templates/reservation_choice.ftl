<#include "base.ftl"/>
<#include "form.ftl" />

<#-- Build the title -->
<#assign title>
<@_ "reservation.create" "Create Reservation"/>
</#assign>

<#-- Build the page -->
<@userbase title>
<h1><@_ "reservation.permission" "Restricted Items:" /></h1>
<@_ "reservation.permissionMsg" "The following items are restricted and require a request for permission to be filed before reserving:"/>
<ul>
  <#assign pids = ""/>
  <#list holdingReservationsRestricted as hr>
    <#assign h = hr.holding>
    <#assign pids = pids + h.record.pid?url+delivery.pidSeparator>

    <li>${h.record.toString()?html}</li>
  </#list>
</ul>

<form action="${rc.contextPath}/permission/createform/${pids?url}" method="GET" target="_blank">
  <input type="submit" value="<@_ "reservation.reqPermission" "Request Permission"/>"/>
</form>

<#if holdingReservationsOpen?has_content>
    <h1><@_ "reservation.allowed" "Open Items:" /></h1>
    <@_ "reservation.openMsg" "The following items are freely available and can be reserved right now:"/>
    <ul>
      <#assign pids = {}/>
      <#list holdingReservationsOpen as hr>
        <#assign h = hr.holding>

        <#if pids[h.record.pid]??>
          <#assign pids = pids + {h.record.pid : (pids[h.record.pid] +  delivery.holdingSeparator + h.signature?url) }>
        <#else>
          <#assign pids = pids + {h.record.pid :  h.signature?url}>
        </#if>

        <li>${h.record.toString()?html}</li>
      </#list>
    </ul>

    <#assign pidParam = "">
    <#list pids?keys as k>
      <#assign pidParam = pidParam + k + delivery.holdingSeparator + pids[k] + delivery.pidSeparator>
    </#list>

    <form action="${rc.contextPath}/reservation/createform/${pidParam?url}" method="GET" target="_blank">
      <input type="submit" value="<@_ "reservation.create" "Create Reservation"/>"/>
    </form>
</#if>

<h1><@_ "reservation.codes" "Permission codes" />:</h1>

<#if error?? >
    <p class="error"><@_ "reservation.error."+error error /></p>
</#if>

<p><@_ "reservation.codesMsg" "If you have received permission for one or more items, please add the codes you received."/></p>
<#if reservation.permissions?has_content>
  <@_ "reservation.codesAdded" "The following codes have been added" />:
  <ul>
  <#list reservation.permissions as permission>
      <li>${permission.code}</li>
  </#list>
  </ul>
</#if>
<form action="#" method="GET">
  <#list reservation.permissions as permission>
    <input type="hidden" name="codes" value="${permission.code}"/>
  </#list>

  <input type="text" class="code" name="codes"/>
  <input type="submit" value="<@_ "reservation.validateCode" "Validate code"/>"/>
</form>
</@userbase>
