<#include "base.ftl"/>
<#include "form.ftl"/>

<@userbase "Error">
  <h1>${_("permission.error", "An error has occurred creating a permission request:")}</h1>
  <p>${_("permission.error."+error, error)}</p>
</@userbase>
