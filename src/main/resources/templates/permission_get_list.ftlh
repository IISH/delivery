<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=_("permissionList.title", "Permission RequestOverview")/>

<@base title>
  <h1>${title}</h1>

  <fieldset class="actions">
    <legend>Filter:</legend>

    <form action="" method="GET">
      <#-- Generate hidden input for already existing GET vars -->
      <#list RequestParameters?keys as k>
          <#if k!="search" && k!="from_date" && k!="to_date" && k!="permission" && k!="page_len" && k!="page">
            <input type="hidden" name="${k}" value="${RequestParameters[k]}"/>
          </#if>
      </#list>

      <ul>
        <li>
          <label for="search_filter">
            ${_("permissionList.search", "Search")}
          </label>
          <input type="text" id="search_filter" name="search" value="${(RequestParameters["search"]!"")?trim}"/>
        </li>

        <li>
          <label for="permission_filter">
            ${_("recordPermission.granted", "Permission")}
          </label>

          <select id="permission_filter" name="permission">
            <#assign isSelected = !RequestParameters["permission"]?? || RequestParameters["permission"] == ""/>
            <option value=""<#if isSelected> selected="selected"</#if>>
              ${_("permissionList.allPermission", "Permission N/A")}
            </option>

            <#assign isSelected = (RequestParameters["permission"]?? && RequestParameters["permission"]?upper_case == 'NULL')/>
            <option value="null"<#if isSelected> selected="selected"</#if>>
              ${_("recordPermission.granted.null", "To decide")}
            </option>

            <#assign isSelected = (RequestParameters["permission"]?? && RequestParameters["permission"]?upper_case == 'TRUE')/>
            <option value="true"<#if isSelected> selected="selected"</#if>>
              ${_("recordPermission.granted.true", "Granted")}
            </option>

            <#assign isSelected = (RequestParameters["permission"]?? && RequestParameters["permission"]?upper_case == 'FALSE')/>
            <option value="false"<#if isSelected> selected="selected"</#if>>
              ${_("recordPermission.granted.false", "Denied")}
            </option>
          </select>
        </li>

        <li>
          <label for="from_date_filter">
            ${_("permissionList.dateFrom", "From")}
          </label>
          <input type="text" maxlength="10" id="from_date_filter" name="from_date" class="filter_date"
                 value="${(RequestParameters["date"]!RequestParameters["from_date"]!"")?trim}"/>
        </li>

        <li>
          <label for="to_date_filter">
            ${_("permissionList.dateUpTo", "Up To")}
          </label>
          <input type="text" maxlength="10" id="to_date_filter" name="to_date" class="filter_date"
                 value="${(RequestParameters["date"]!RequestParameters["to_date"]!"")?trim}"/>
        </li>

        <li>
          <label for="page_len_filter">
            ${_("pageListHolder.nrResultsPerPage", "Amount of Results per Page")}
          </label>
          <select id="page_len_filter" name="page_len">
              <#list 1..(delivery.permissionMaxPageLen?number/delivery.permissionPageStepSize?number)?floor as i>
                <#assign pageSize = (i*delivery.permissionPageStepSize?number)?floor/>
                <#assign isSelected = (RequestParameters["page_len"]??
                  && RequestParameters["page_len"]?number == pageSize)
                  || (!RequestParameters["page_len"]?? && delivery.permissionPageLen?number == pageSize)/>

                <option value="${pageSize}"<#if isSelected> selected="selected"</#if>>${pageSize}</option>
              </#list>
          </select>
        </li>
      </ul>

      <input type="submit" value="${_("permissionList.search", "Search")}"/>
    </form>
  </fieldset>

  <#if recordPermissions?size == 0>
    <span class="bignote">${_("search.notfound", "No results...")}</span>
  <#else>
    <table class="overview">
      <thead>
      <tr>
        <th></th>
        <th>${_("holding.record", "Item")}</th>
        <th><@sortLink "visitor_name">${_("permission.name", "Name")}</@sortLink></th>
        <th><@sortLink "permission">${_("permission.permission", "Permission")}</@sortLink></th>
        <th><@sortLink "date_granted">${_("permission.dateGranted", "Date granted")}</@sortLink></th>
      </tr>
      </thead>
      <tbody>
      <#list recordPermissions as recordPermission>
        <#assign record = recordPermission.record>
        <#assign permission = recordPermission.permission>

        <tr>
          <td>
            <a href="${rc.contextPath}/permission/${permission.id?c}">
              ${_("permissionList.edit", "Administrate")}
            </a>
          </td>

          <td class="leftAligned">${record.toString()}</td>

          <td>${permission.name}</td>

          <td>
            <#if !recordPermission.granted && !recordPermission.dateGranted??>
              ${_("recordPermission.granted.null", "To be reviewed")}
            <#else>
              ${recordPermission.granted?string(_("recordPermission.granted.true", "Granted"), _("recordPermission.granted.false", "Denied"))}
            </#if>
          </td>

          <td>
            <#if recordPermission.dateGranted??>
              ${recordPermission.dateGranted?string(delivery.dateFormat)}
            <#else>
              ${_("recordPermission.granted.null", "To decide")}
            </#if>
          </td>
        </tr>
      </#list>
      </tbody>
    </table>

    <@pageLinks recordPermissionsSize RequestParameters["page_len"]!delivery.requestPageLen?number RequestParameters["page"]!1/>
  </#if>
</@base>
