<#include "base.ftl">

<@base "Home">
  <#if !_sec.isAnonymous() && _sec.ifNotGranted("ROLE_DELIVERY_USER")>
    <h1>${_("delivery.error", "Oops! An error has occurred.")}</h1>
    <p>${_("home_candidate")} <b>${_sec.getUsername()}</b></p>
  </#if>
</@base>
