<#include "../base.ftl"/>

<@userbase "Error">
  <h1>${_("delivery.error", "Oops! An error has occurred.")}</h1>

  <p>${_("home_candidate")}:</p>
  <p><b>${_sec.getUsername()}</b></p>
</@userbase>
