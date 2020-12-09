<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=_("permissionSingle.title", "Permission Request") + " " + permission.id?c/>

<#macro createNode node selected isParent=true>
  <li <#if node.unitId??>id="${node.unitId}"</#if>
      <#if isParent>class="jstree-open"</#if>
      <#if node.children?size == 0>data-jstree='{"icon":"jstree-icon jstree-file"}'</#if>>
    <#if node.children?size gt 0>
      <@createNodeItem node/>

      <ul>
        <#list node.children as nodeChild>
          <@createNode nodeChild selected false/>
        </#list>
      </ul>
    <#else>
      <a href="#" <#if selected?seq_contains(node.unitId) || selected?seq_contains('*')>class="jstree-clicked"</#if>>
        <@createNodeItem node/>
      </a>
    </#if>
  </li>
</#macro>

<#macro createNodeItem node>
  <span class="grey">
    <#if node.unitId??>
      ${node.unitId}
    <#else>
      <#assign first=node.firstChildUnitId!"?"/>
      <#assign last=node.lastChildUnitId!"?"/>
      <#if first == last>${first}<#else>${first} - ${last}</#if>
    </#if>

    &#9642;
  </span>

  <#if node.title??>${node.title}</#if>
</#macro>

<@base title>
  <h1>${title}</h1>

  <ul class="permissionDetails">
    <li><span>${_("permission.name", "Name")}</span> ${permission.name}</li>
    <li><span>${_("permission.email", "E-mail")}</span> ${permission.email}</li>

    <#if permission.address??>
      <li>
        <span>${_("permission.address", "Address")}</span>
        ${permission.address}
      </li>
    </#if>

    <li class="spacing">
      <span>${_("permission.record", "Item")}</span>
      ${permission.record.toString()}
    </li>

    <li class="spacing">
      <span>${_("permission.researchSubject", "Research Subject")}</span>
      <em>${permission.researchSubject}</em>
    </li>

    <li>
      <span>${_("permission.researchOrganization", "Research Organization")}</span>
      <em>${permission.researchOrganization}</em>
    </li>

    <#if permission.explanation??>
      <li>
        <span>${_("permission.explanation", "Explanation/Comments")}</span>
        <em>${permission.explanation}</em>
      </li>
    </#if>

    <li>
      <span>${_("permission.request", "Request for materials")}</span>
      <em>${permission.request}</em>
    </li>

    <li class="spacing"><span>${_("permission.code", "Code")}</span> ${permission.code}</li>
  </ul>

  <form action="process" method="POST">
    <input type="hidden" name="id" value="${permission.id?c}"/>

    <fieldset class="permissionDetails">
      <ul class="form">
        <li>
          <label for="granted" class="field">
            ${_("permission.granted", "Permission")}
          </label>

          <select name="granted" class="permission">
            <option value="null" <#if !permission.dateGranted??>selected="selected"</#if>>
              ${_("permission.granted.null", "To decide")}
            </option>
            <option value="true" <#if permission.dateGranted?? && permission.granted>selected="selected"</#if>>
              ${_("permission.granted.true", "Granted")}
            </option>
            <option value="false" <#if permission.dateGranted?? && !permission.granted>selected="selected"</#if>>
              ${_("permission.granted.false", "Denied")}
            </option>
          </select>
        </li>

        <li>
          <label for="motivation" class="field">
            ${_("permission.motivation", "Motivation")}

            <br/>

            <span class="note">
              (${_("permissionSingle.motivationLanguage", "Please specify in")}
              ${_("language.${permission.requestLocale}", "English")})
            </span>
          </label>

          <textarea id="motivation" name="motivation"><#if permission.motivation??>${permission.motivation}</#if></textarea>
        </li>
      </ul>

      <#if permission.record.externalInfo.inventory??>
        <div class="on-granted hidden">
          <h3>Permission granted on</h3>

          <input type="hidden" name="invNosGranted" value="${permission.invNosGranted?join('__')}"/>

          <div id="inventory">
            <ul>
              <@createNode permission.record.externalInfo.inventory permission.invNosGranted/>
            </ul>
          </div>
        </div>
      </#if>
    </fieldset>

    <input type="submit" name="save" value="${_("permissionSingle.save", "Save")}"/>
    <input type="submit" name="saveandemail" value="${_("permissionSingle.saveAndEmail", "Save and email")}"/>

    <#if _sec.ifAllGranted("ROLE_PERMISSION_DELETE")>
      <input type="submit" name="delete"
             onClick="return confirm('${_("permissionSingle.confirmDelete", "Deletion of this permission request, including all the individual permissions set, is permanent. Are you sure you want to delete this permission request?")}');"
             value="${_("permissionSingle.delete", "Delete")}"/>
    </#if>
  </form>
</@base>
