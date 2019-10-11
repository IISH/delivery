<#include "base.ftl"/>
<#include "form.ftl" />

<#-- Build the title -->
<#assign title>
<@_ "permissionList.title" "Permission RequestOverview"/>
</#assign>

<#-- Build the page -->
<@base "${title}">
<h1>${title}</h1>


<fieldset class="actions">
<legend>Filter:</legend>
<form action="" method="GET">
  <#-- Generate hidden input for already existing GET vars -->
  <#list RequestParameters?keys as k>
  <#if k!="search" && k!="from_date" && k!="to_date" && k!="permission" && k!="page_len" && k!="page">
  <input type="hidden" name="${k?html}" value="${RequestParameters[k]?html}"/>
  </#if>
  </#list>
  <ul>
    <li>
      <#assign search_value>
        <#if RequestParameters["search"]??>
          ${RequestParameters["search"]?html}
        </#if>
      </#assign>
      <label for="search_filter"><@_ "permissionList.search" "Search"/>
      </label>
      <input type="text" id="search_filter" name="search"
             value="${search_value?trim}" />
    </li>
      <li>
          <@filterPermissions />
      </li>
      <li>
          <#assign from_date_value>
          <#-- The date field has priority over from_date -->
              <#if RequestParameters["date"]??>
              ${RequestParameters["date"]?html}
              <#elseif RequestParameters["from_date"]??>
              ${RequestParameters["from_date"]?html}
              </#if>
          </#assign>
          <label for="from_date_filter"><@_ "permissionList.date" "Date"/>
          </label>
		  <strong><@_ "misc.from" "from"/></strong> <input type="text" maxlength="10" id="from_date_filter" name="from_date" value="${from_date_value?trim}" class="filter_date" />

          <#assign to_date_value>
          <#-- The date field has priority over to_date -->
              <#if RequestParameters["date"]??>
              ${RequestParameters["date"]?html}
              <#elseif RequestParameters["to_date"]??>
              ${RequestParameters["to_date"]?html}
              </#if>
          </#assign>
		  <strong><@_ "misc.to" "to"/></strong>
          <input type="text" maxlength="10" id="to_date_filter" name="to_date" value="${to_date_value?trim}" class="filter_date" />
      </li>
    <li>
        <@filterResultsPerPage />
    </li>
  </ul>
    <#assign searchLabel>
    <@_ "permissionList.search" "Search"/>
    </#assign>
    <input type="submit" value="${searchLabel}"/>
</form>
</fieldset>

<#assign granted_true>
<@_ "recordPermission.granted.true" "Granted"/>
</#assign>
<#assign granted_false>
<@_ "recordPermission.granted.false" "Denied"/>
</#assign>
<#assign granted_null>
<@_ "recordPermission.granted.null" "To be reviewed"/>
</#assign>


<#if recordPermissions?size == 0>
<span class="bignote"><@_ "search.notfound" "No results..."/></span>
<#else>
<table class="overview">
  <thead>
  <tr>
    <th></th>
    <th><@_ "holding.record" "Item"/></th>
    <th><@sortLink "visitor_name"><@_ "permission.name" "Name"/></@sortLink></th>
    <th><@sortLink "date_requested"><@_ "permission.dateRequested" "Date requested"/></@sortLink></th>
    <th><@sortLink "permission"><@_ "permission.permission" "Permission"/></@sortLink></th>
    <th><@sortLink "date_granted"><@_ "permission.dateGranted" "Date granted"/></@sortLink></th>
  </tr>
  </thead>
  <tbody>
  <#list recordPermissions as recordPermission>
    <#assign record = recordPermission.record>
    <#assign permission = recordPermission.permission>
  <tr>
    <td>
      <a href="${rc.contextPath}/permission/${permission.id?c}">
      <@_ "permissionList.edit" "Administrate"/>
      </a>
    </td>
    <td class="leftAligned">${record.toString()?html}</td>
    <td>${permission.name?html}</td>
    <td>
    <#if permission.dateRequested??>
    ${permission.dateRequested?string(delivery.dateFormat)}
    </#if>
    </td>
    <td>
    <#if !recordPermission.granted && !recordPermission.dateGranted??>
        ${granted_null}
    <#else>
        ${recordPermission.granted?string(granted_true,granted_false)}
    </#if>
    </td>
    <td>
    <#if recordPermission.dateGranted??>
        ${recordPermission.dateGranted?string(delivery.dateFormat)}
    <#else>
        &nbsp;
    </#if>
    </td>
  </tr>
  </#list>
  </tbody>
</table>
<@pageLinks recordPermissionsSize RequestParameters["page_len"]!delivery.requestPageLen?number RequestParameters["page"]!1 />
</#if>
</@base>

