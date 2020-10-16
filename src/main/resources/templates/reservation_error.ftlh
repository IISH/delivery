<#include "base.ftl"/>
<#include "form.ftl"/>

<@userbase "Error">
  <h1>${_("reservation.error", "An error has occurred creating a reservation:")}</h1>
  <p class="error">${_("reservation.error."+error, error)}</p>
</@userbase>
