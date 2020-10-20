<#function _ ident msg>
  <#return msgResolver.getMessage(ident, msg)/>
</#function>

<#if callback??>
${callback}([
<#else>
[
</#if>
  <#list records as record>
  {
    "pid" : "${record.pid?js_string}",
    "title" : "${record.title?js_string}",
    "publicationStatus": "${record.publicationStatus}",
    "restriction": "${record.restriction}",
    "openForReproduction": ${record.openForReproduction?c},
     <#if record.copyright??>
      "copyright": "${record.copyright?js_string}",
     </#if>

    "holdings" : [
      <#list record.holdings as h>
      {
        "signature" : "${h.signature?js_string}",
        "status" : "${h.status}",
        "usageRestriction" : "${h.usageRestriction}"
      }
      <#if h_has_next>,</#if>
      </#list>
    ],

    "reservedChilds" : [
      <#list reservedChilds[record.pid] as reservedChildRecord>
        <#assign childHolding = reservedChildRecord.holdings?first/>
        "${childHolding.signature?js_string}"<#if reservedChildRecord_has_next>,</#if>
      </#list>
   ]
  }<#if record_has_next>,</#if>
  </#list>
]
<#if callback??>
);
</#if>
