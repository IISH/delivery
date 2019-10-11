<#include "base.ftl"/>
<#include "form.ftl" />

<#-- Build the title -->
<#assign title>
<@_ "reproductionMaterials.noRequestsAndTotalAmount" "Number of requests and total amount paid"/>
</#assign>

<#-- Build the page -->
<@base "${title}">
<h1>${title}</h1>

<fieldset class="actions">
<legend>Filter:</legend>
<form action="" method="GET">
  <#-- Generate hidden input for already existing GET vars -->
  <#list RequestParameters?keys as k>
    <#if k!="from_date" && k!="to_date">
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
        <@_ "reproductionList.dateFrom" "From date"/>
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
        <@_ "reproductionList.dateUpTo" "Up To"/>
      </label>
      <input type="text" maxlength="10" id="to_date_filter" name="to_date"
             value="${to_date_value?trim}" class="filter_date" />
    </li>
  </ul>
    <#assign searchLabel>
    <@_ "reproductionList.search" "Search"/>
    </#assign>
    <input type="submit" value="${searchLabel}"/>
</form>
</fieldset>

<#if tuplesMaterials?size gt 0>
  <table class="overview">
  <thead>
  <tr>
    <th><@_ "record.externalInfo.materialType" "Material type"/></th>
    <th><@_ "reproductionMaterials.noRequests" "Number of requests"/></th>
  </tr>
  </thead>
  <tbody>
    <#list tuplesMaterials as tuple>
    <tr>
      <td><@_ "record.externalInfo.materialType.${tuple.get('material')?upper_case}" "${tuple.get('material')?upper_case}" /></td>
      <td>${tuple.get('noRequests')?html}</td>
    </tr>
    </#list>
  </tbody>
  </table>
</#if>

<#if tuplePayedAmounts?size gt 0>
  <ul class="reproduction_totals">
  <#list tuplePayedAmounts as tuple>
    <li>
      <strong>
        ${tuple.get('totalItems')?html} <@_ "items" "items"/>
        <@_ "inclusive" "incl."/> ${tuple.get('btwPercentage')?c}&percnt; <@_ "btw" "BTW"/>:
      </strong>

      <ul>
        <li>
          <strong><@_ "total" "Total"/>:</strong>
          &euro; ${tuple.get('sumTotalAmount')?string("0.00")}
        </li>

        <li>
          <strong><@_ "holdingReproductions.discount" "Computed discount"/>:</strong>
          &euro; ${tuple.get('sumDiscount')?string("0.00")}
        </li>

        <li>
          <strong><@_ "holdingReproductions.btw" "Computed BTW"/>:</strong>
          &euro; ${tuple.get('sumBtwPrice')?string("0.00")}
        </li>
      </ul>
    </li>
  </#list>
  </ul>
</#if>

<a href="${rc.contextPath}/reproduction/excel?from_date=${from_date_value?trim}&to_date=${to_date_value?trim}">
  <@_ "reproductionList.downloadPayedXls" "Download an overview of paid reproductions for this period (XLS)"/>
</a>
</@base>
