<#include "../base.ftl"/>
<#include "../form.ftl"/>

<@userbase "Error">
  <h1>${_("delivery.error", "Oops! An error has occurred.")}</h1>

  ${_("delivery.serverError", "A server error has occurred.")}

  <p><a href="mailto:${_("iisg.email", "")}">${_("iisg.email", "")}</a></p>
</@userbase>
