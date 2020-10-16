<#include "mail.ftl">
<@mail to="${permission.name}">
${_("permissionMail.refusedMessage", "We are sorry, but the permission request has been refused by the owner of the archive.")}


--- ${_("permission.recordPermissions", "Permissions per Record")} ---
<#list permission.recordPermissions as rp>
<#assign info = rp.record.externalInfo>
${_("record.title", "Title")}: ${rp.record.toString()}
${_("recordPermission.granted", "Permission")}: ${rp.granted?string(_("recordPermission.granted.true", "Granted"), _("recordPermission.granted.false", "Denied"))}
<#if rp.motivation??>${_("recordPermission.motivation", "Motivation")}: ${rp.motivation}</#if>
<#if rp_has_next>--</#if>
</#list>
</@mail>
