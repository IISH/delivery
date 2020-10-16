<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=_("reservationPrintFailure.title", "Printing (partially) failed")/>

<@base title>
  <h1>${title}</h1>

  <p>${_("reservationPrintFailure.message", "Failed to print, please try again.")}</p>
  <p><a href="<@paramUrl {} "/reservation/"/>">${_("reservationPrintFailure.back", "Back")}</a></p>
</@base>
