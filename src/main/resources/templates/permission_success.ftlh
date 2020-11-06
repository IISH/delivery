<#import "spring.ftl" as spring/>
<#include "base.ftl">

<#assign title=_("permission.success", "Permission request placed successfully.")/>

<@userbase title>
  <section>
    <heading>
      <h1>${title}</h1>
    </heading>

    ${_("permission.successMsg", "Permission has been requested. You will be contacted with the decision as soon as possible.")}

    <#if error??>
      <p class="error">${_("permission.error."+error, error)}</p>
    </#if>

    <p>${_("permission.backToSearch", "Close this tab to continue your search.")}</p>
  </section>
</@userbase>
