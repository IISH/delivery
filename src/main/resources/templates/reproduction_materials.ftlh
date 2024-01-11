<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=_("reproductionMaterials.noRequestsAndTotalAmount", "Number of requests and total amount paid")/>

<@base title>
  <h1>${title}</h1>

  <fieldset class="actions">
    <legend>Filter:</legend>

    <form action="" method="GET">
      <#-- Generate hidden input for already existing GET vars -->
      <#list RequestParameters?keys as k>
        <#if k!="from_date" && k!="to_date">
          <input type="hidden" name="${k}" value="${RequestParameters[k]}"/>
        </#if>
      </#list>

      <ul>
        <li>
          <label for="from_date_filter">
              ${_("reproductionList.dateFrom", "From")}
          </label>
          <input type="text" maxlength="10" id="from_date_filter" name="from_date"
                 value="${(RequestParameters["from_date"]!.now?string("yyyy-MM-dd")!"")?trim}" class="filter_date"/>
        </li>

        <li>
          <label for="to_date_filter">
              ${_("reproductionList.dateUpTo", "Up To")}
          </label>
          <input type="text" maxlength="10" id="to_date_filter" name="to_date" class="filter_date"
                 value="${(RequestParameters["to_date"]!.now?string("yyyy-MM-dd"))?trim}"/>
        </li>
      </ul>

      <input type="submit" value="${_("reproductionList.search", "Search")}"/>
    </form>
  </fieldset>

  <#if tuplesMaterials?size gt 0>
    <table class="overview">
      <thead>
      <tr>
        <th>${_("record.externalInfo.materialType", "Material type")}</th>
        <th>${_("reproductionMaterials.noRequests", "Number of requests")}</th>
      </tr>
      </thead>

      <tbody>
      <#list tuplesMaterials as tuple>
        <tr>
          <td>${_("record.externalInfo.materialType.${tuple.get('material')?upper_case}", "${tuple.get('material')?upper_case}")}</td>
          <td>${tuple.get('noRequests')}</td>
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
            ${tuple.get('totalItems')} ${_("items", "items")}
            <#if tuple.get('btwPercentage')??>${_("inclusive" "incl.")} ${tuple.get('btwPercentage')?c}&percnt; ${_("btw", "BTW")}:</#if>
          </strong>

          <ul>
            <li>
              <strong>${_("total", "Total")}:</strong>
              &euro; ${tuple.get('sumTotalAmount')?string("0.00")}
            </li>

            <li>
              <strong>${_("holdingReproductions.discount", "Computed discount")}:</strong>
              &euro; ${tuple.get('sumDiscount')?string("0.00")}
            </li>

            <li>
              <strong>${_("holdingReproductions.btw", "Computed BTW")}:</strong>
              &euro; ${tuple.get('sumBtwPrice')?string("0.00")}
            </li>
          </ul>
        </li>
      </#list>
    </ul>
  </#if>

  <a href="${rc.contextPath}/reproduction/excel?from_date=${(RequestParameters["from_date"]!.now?string("yyyy-MM-dd")!"")?trim}&to_date=${(RequestParameters["to_date"]!.now?string("yyyy-MM-dd"))?trim}">
    ${_("reproductionList.downloadPayedXls", "Download an overview of paid reproductions for this period (XLS)")}
  </a>
</@base>
