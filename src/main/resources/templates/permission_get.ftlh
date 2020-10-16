<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=_("permissionSingle.title", "Permission Request") + " " + permission.id?c/>

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

    <li class="spacing"><span>${_("permission.code", "Code")}</span> ${permission.code}</li>
  </ul>

  <h3>${_("permission.recordPermissions", "Permissions per record")}:</h3>

  <form action="process" method="POST">
    <input type="hidden" name="id" value="${permission.id?c}"/>

    <#list permission.recordPermissions as rp>
      <fieldset class="recordPermissionDetails">
        <#assign info = rp.record.externalInfo>
        <legend>${rp.record.toString()}</legend>

        <ul>
          <li>
            <select name="granted_${rp.id?c}" <#if _sec.ifNotGranted("ROLE_PERMISSION_MODIFY")>disabled="disabled"</#if>>
              <option value="null" <#if !rp.dateGranted??>selected="selected"</#if>>
                ${_("recordPermission.granted.null", "To decide")}
              </option>
              <option value="true" <#if rp.dateGranted?? && rp.granted>selected="selected"</#if>>
                ${_("recordPermission.granted.true", "Granted")}
              </option>
              <option value="false" <#if rp.dateGranted?? && !rp.granted>selected="selected"</#if>>
                ${_("recordPermission.granted.false", "Denied")}
              </option>
            </select>

            <#if rp.record.parent??>
              <label>
                <input type="checkbox" name="parent_${rp.id?c}"/>
                ${_("recordPermission.parent", "Allow/Deny on a collection level")}
              </label>
            </#if>
          </li>

          <li class="motivation">
            <label for="motivation_${rp.id?c}">
              ${_("recordPermission.motivation", "Motivation")} <br/>
              <span class="note">
                (${_("permissionSingle.motivationLanguage", "Please specify in")}
                ${_("language.${permission.requestLocale}", "English")})
              </span>
            </label>

            <textarea id="motivation_${rp.id?c}"
                      name="motivation_${rp.id?c}"><#if rp.motivation??>${rp.motivation}</#if></textarea>
          </li>

          <#if _sec.ifAllGranted("ROLE_PERMISSION_MODIFY")>
            <li>
              <a href="${rc.contextPath}/record/editform/${rp.record.pid?url}" target="_blank">
                  ${_("permissionSingle.toRecord", "Go to Record")}
              </a>
            </li>
          </#if>
        </ul>
      </fieldset>
    </#list>

    <#if _sec.ifAllGranted("ROLE_PERMISSION_MODIFY")>
      <input type="submit" name="save" value="${_("permissionSingle.save", "Save")}"/>
      <input type="submit" name="saveandemail" value="${_("permissionSingle.saveAndEmail", "Save and email")}"/>

      <#if _sec.ifAllGranted("ROLE_PERMISSION_DELETE")>
        <input type="submit" name="delete"
               onClick="return confirm('${_("permissionSingle.confirmDelete", "Deletion of this permission request, including all the individual permissions set, is permanent. Are you sure you want to delete this permission request?")}');"
               value="${_("permissionSingle.delete", "Delete")}"/>
      </#if>
    </#if>
  </form>
</@base>
