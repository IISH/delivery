<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=_("reservationMaterials.noRequests", "Number of requests")/>

<@base title>
  <h1>${title}</h1>

  <fieldset class="actions">
    <legend>Filter:</legend>

    <form action="" method="GET">
      <#-- Generate hidden input for already existing GET vars -->
      <#list RequestParameters?keys as k>
        <#if k!="from_date" && k!="to_date" && k!="material">
          <input type="hidden" name="${k}" value="${RequestParameters[k]}"/>
        </#if>
      </#list>

      <ul>
        <li>
          <label for="from_date_filter">
            ${_("reservationList.dateFrom", "From")}
          </label>
          <input type="text" maxlength="10" id="from_date_filter" name="from_date" class="filter_date"
                 value="${(RequestParameters["from_date"]!.now?string("yyyy-MM-dd"))?trim}"/>
        </li>

        <li>
          <label for="to_date_filter">
            ${_("reservationList.dateUpTo", "Up To")}
          </label>
          <input type="text" maxlength="10" id="to_date_filter" name="to_date" class="filter_date"
                 value="${(RequestParameters["to_date"]!.now?string("yyyy-MM-dd"))?trim}"/>
        </li>

        <li>
          <label for="material_filter">
            ${_("record.externalInfo.materialType", "Material")}
          </label>

          <select id="material_filter" name="material">
            <#assign isSelected = !RequestParameters["material"]?? || RequestParameters["material"] == ""/>
            <option value=""<#if isSelected> selected="selected"</#if>>
              ${_("reservationList.allMaterialTypes", "Material N/A")}
            </option>

            <#list material_types?keys as k>
              <#assign isSelected = (RequestParameters["material"]?? && RequestParameters["material"]?upper_case == k)/>
              <option value="${k?lower_case}"<#if isSelected> selected="selected"</#if>>
                ${_("record.externalInfo.materialType.${k}", "${k}")}
              </option>
            </#list>
          </select>
        </li>
      </ul>

      <input type="submit" value="${_("reservationList.search", "Search")}"/>
    </form>
  </fieldset>

  <#if materialTuples?size == 0 && signatureTuples?size == 0>
    <span class="bignote">${_("search.notfound", "No results...")}</span>
  <#else>
    <table class="overview">
      <thead>
        <tr>
          <th>${_("record.externalInfo.materialType", "Material type")}</th>
          <th>${_("reservationMaterials.noRequests", "Number of requests")}</th>
        </tr>
      </thead>

      <tbody>
        <#list materialTuples as tuple>
          <tr>
            <td>${_("record.externalInfo.materialType.${tuple.get('material')?upper_case}", "${tuple.get('material')?upper_case}")}</td>
            <td>${tuple.get('noRequests')}</td>
          </tr>
        </#list>
      </tbody>
    </table>

    <table class="overview with-sub-rows">
      <thead>
        <tr>
          <th>${_("holding.signature", "Signature")}</th>
          <th>${_("record.title", "Title")}</th>
          <th>${_("reservationMaterials.noRequests", "Number of requests")}</th>
        </tr>
      </thead>
      <tbody>
        <#list parentSignaturesMap?keys as parentSignature>
          <tr>
            <td>${parentSignature}</td>
            <td>${parentSignaturesMap[parentSignature].title}</td>
            <td>${parentSignaturesMap[parentSignature].count}</td>
          </tr>

          <#list signatureTuples as signatureTuple>
            <#if signatureTuple.get("parentSignature")?? && signatureTuple.get("parentSignature") == parentSignature>
              <tr class="sub-row hidden">
                <td>${signatureTuple.get("signature")}</td>
                <td>&nbsp;</td>
                <td>${signatureTuple.get("numberOfRequests")}</td>
              </tr>
            </#if>
          </#list>
        </#list>
      </tbody>
    </table>
  </#if>
</@base>
