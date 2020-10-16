<#import "spring.ftl" as spring/>
<#include "base.ftl">

<#assign title=_("permission.success", "Permission request placed successfully.")/>

<@userbase title>
  <section>
    <heading>
      <h1>${title}</h1>
    </heading>

    <section>
      <ul class="items">
        <#list records as rec>
          <li>${rec.toString()}</li>
        </#list>
      </ul>
    </section>

    ${_("permission.successMsg", "Permission has been requested. You will be contacted with the decision as soon as possible.")}

    <#if error??>
      <p class="error">${_("permission.error."+error, error)}</p>
    </#if>

    <p>${_("permission.backToSearch", "Close this tab and return to Search for new requests")}</p>
  </section>
</@userbase>
