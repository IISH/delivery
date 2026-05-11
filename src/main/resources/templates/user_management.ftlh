<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=_("userList.title", "User Management")/>

<@base title>
  <h1>${title}</h1>

  <ul class="user_list">
    <#list users as user>
      <li>
        <span class="user">
          ${user.username}
        </span>

        <form method="POST" action="" <#if user.username == _sec.username> style='background-color: darkgray'</#if>>
            <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
            <input type="hidden" name="action" value="chgrp"/>
          <input type='hidden' name="user" value="${user.id?c}"/>

          <select multiple name="groups"<#if user.username == _sec.username> disabled="disabled"</#if>>
            <#list groups as group>
              <option value="${group.id?c}"<#if user.groups?seq_contains(group)> selected</#if>>
                ${group.name}
              </option>
            </#list>
          </select>

          <#if user.username != _sec.username>
            <input type="submit" value="${_("user.chgrp", "Change Groups")}"/>
          </#if>
        </form>
      </li>
    </#list>
  </ul>
</@base>
