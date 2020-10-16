<#include "base.ftl"/>

<#assign title=_("homerecord.title", "Edit Records")/>

<@base title>
  <h1>${title}</h1>

  <fieldset class="actions">
    <form action="" method="GET">
      <label class="field" for="pid">
        ${_("editrecord.byPid", "Search by PID:")}
      </label>

      <input type="text" class="field" name="pid" id="pid" value=""/>
      <input type="submit" class="field" name="searchPid" value="${_("editrecord.searchPid", "Search")}"/>
    </form>

    <form action="" method="GET">
      <label class="field" for="title">
        ${_("editrecord.byTitle", "Search for title:")}
      </label>

      <input type="text" class="field" name="title" id="title" value="${recordTitle!""}"/>
      <input type="submit" class="field" name="searchApi" value="${_("editrecord.searchTitle", "Search")}"/>
    </form>
  </fieldset>

  <#if pageChunk?? && pageChunk.results??>
    <#if pageChunk.results?size == 0>
      <span class="bignote">
        ${_("search.notfound", "No results...")}
      </span>
    <#else>
      <span class="bignote">
        ${pageChunk.totalResultCount} ${_("search.nrOfResults", "records matched your query.")}
      </span>

      <table class="searchRecord">
        <thead>
        <tr>
          <th>${_("record.title", "")}</th>
          <th>PID</th>
        </tr>
        </thead>
        <tbody>
        <#list pageChunk.results?keys as key>
          <tr>
            <td>${pageChunk.results[key]}</td>
            <td><a href="${rc.contextPath}/record/editform/${key?url}">${key}</a></td>
          </tr>
        </#list>
        </tbody>
      </table>

      <@pageApiLinks pageChunk />
    </#if>
  </#if>
</@base>
