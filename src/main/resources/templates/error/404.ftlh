<#include "../base.ftl"/>
<#include "../form.ftl"/>

<@userbase "Error">
  <h1>${_("delivery.error", "Oops! An error has occurred.")}</h1>

  ${_("delivery.pageNotFound", "The page you are looking for cannot be found.")}

  <p><a href="mailto:${_("iisg.email", "")}">${_("iisg.email", "")}</a></p>
</@userbase>
