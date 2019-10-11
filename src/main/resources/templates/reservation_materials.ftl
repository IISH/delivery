<#include "base.ftl"/>
<#include "form.ftl" />

<#-- Build the title -->
<#assign title>
<@_ "reservationMaterials.noRequests" "Number of requests"/>
</#assign>

<#-- Build the page -->
<@base "${title}">
<h1>${title}</h1>

<fieldset class="actions">
<legend>Filter:</legend>
<form action="" method="GET">
  <#-- Generate hidden input for already existing GET vars -->
  <#list RequestParameters?keys as k>
    <#if k!="from_date" && k!="to_date" && k!="material">
      <input type="hidden" name="${k?html}" value="${RequestParameters[k]?html}"/>
    </#if>
  </#list>
  <ul>
    <li>
      <#assign from_date_value>
        <#if RequestParameters["from_date"]??>
          ${RequestParameters["from_date"]?html}
        <#else>
          ${.now?string("yyyy-MM-dd")}
        </#if>
      </#assign>
      <label for="from_date_filter">
        <@_ "reservationList.dateFrom" "From date"/>
      </label>
      <input type="text" maxlength="10" id="from_date_filter" name="from_date"
             value="${from_date_value?trim}" class="filter_date" />
    </li>
    <li>
      <#assign to_date_value>
        <#if RequestParameters["to_date"]??>
          ${RequestParameters["to_date"]?html}
        <#else>
          ${.now?string("yyyy-MM-dd")}
        </#if>
      </#assign>
      <label for="to_date_filter">
        <@_ "reservationList.dateUpTo" "Up To"/>
      </label>
      <input type="text" maxlength="10" id="to_date_filter" name="to_date"
             value="${to_date_value?trim}" class="filter_date" />
    </li>
    <li>
      <label for="material_filter"><@_ "record.externalInfo.materialType" "Material"/></label>
      <select id="material_filter" name="material">
        <option value=""
        <#if !RequestParameters["material"]?? ||
        RequestParameters["material"] == "">
        selected="selected"</#if>>
        <@_ "reservationList.allMaterialTypes" "Material N/A"/>
        </option>

        <#list material_types?keys as k>
        <option value="${k?lower_case}"
        <#if (RequestParameters["material"]?? &&
        RequestParameters["material"]?upper_case == k)>
        selected="selected"</#if>>
        <@_ "record.externalInfo.materialType.${k}" "${k}" />
        </option>
        </#list>
      </select>
    </li>
  </ul>
    <#assign searchLabel>
    <@_ "reservationList.search" "Search"/>
    </#assign>
    <input type="submit" value="${searchLabel}"/>
</form>
</fieldset>

<#if materialTuples?size == 0 && signatureTuples?size == 0>
<span class="bignote"><@_ "search.notfound" "No results..."/></span>
<#else>
<table class="overview">
  <thead>
  <tr>
    <th><@_ "record.externalInfo.materialType" "Material type"/></th>
	<th><@_ "reservationMaterials.noRequests" "Number of requests"/></th>
  </tr>
  </thead>
  <tbody>
  <#list materialTuples as tuple>
  <tr>
    <td><@_ "record.externalInfo.materialType.${tuple.get('material')?upper_case}" "${tuple.get('material')?upper_case}" /></td>
    <td>${tuple.get('noRequests')?html}</td>
  </tr>
  </#list>
  </tbody>
</table>

<table class="overview with-sub-rows">
  <thead>
  <tr>
    <th><@_ "holding.signature" "Signature"/></th>
    <th><@_ "record.title" "Title"/></th>
    <th><@_ "reservationMaterials.noRequests" "Number of requests"/></th>
  </tr>
  </thead>
  <tbody>
  <#list parentSignaturesMap?keys as parentSignature>
  <tr>
    <td>${parentSignature?html}</td>
    <td>${parentSignaturesMap[parentSignature].title?html}</td>
    <td>${parentSignaturesMap[parentSignature].count?html}</td>
  </tr>
  <#list signatureTuples as signatureTuple>
    <#if signatureTuple.get("parentSignature")?? && signatureTuple.get("parentSignature") == parentSignature>
  <tr class="sub-row hidden">
    <td>${signatureTuple.get("signature")?html}</td>
    <td>&nbsp;</td>
    <td>${signatureTuple.get("numberOfRequests")?html}</td>
  </tr>
    </#if>
  </#list>
  </#list>
  </tbody>
</table>
</#if>

</@base>
