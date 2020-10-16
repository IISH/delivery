<#include "mail.ftl">
<@mail to="${permission.name}">
${_("permissionMail.approvedMessage", "Your permission request has been (partially) approved. You can create a reservation with the link below.")}

${delivery.urlSelf}/reservation/createform/<#list permission.recordPermissions as rp><#if rp.granted><#if rp.originalRequestPids??>${rp.originalRequestPids?url}<#else>${rp.record.pid?url}</#if><#if rp_has_next>,</#if></#if></#list>?codes=${permission.code}&locale=${locale}

${_("permissionMail.code", "Code")}: ${permission.code}

--- ${_("permission.recordPermissions", "Permissions per Record")} ---
<#list permission.recordPermissions as rp>
<#assign info = rp.record.externalInfo>
${_("record.title", "Title")}: ${rp.record.toString()}
${_("recordPermission.granted", "Permission")}: ${rp.granted?string(_("recordPermission.granted.true", "Granted"), _("recordPermission.granted.false", "Denied"))}
<#if rp.motivation??>${_("recordPermission.motivation", "Motivation")}: ${rp.motivation}</#if>
<#if rp_has_next>--</#if>
</#list>

</@mail>
