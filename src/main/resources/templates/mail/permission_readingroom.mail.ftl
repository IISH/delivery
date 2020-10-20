<#include "mail.ftl">
<@mailRaedingRoom>
${_("permissionMail.readingRoomMessage", "A visitor has sent a request for permission to access a restricted archive.")}

URL: ${delivery.urlSelf}/permission/${permission.id?c}

* ${_("permission.name", "Name")}: ${permission.name}
* ${_("permission.email", "E-mail")}: ${permission.email}
<#if permission.address??>* ${_("permission.address", "Address")}:
${permission.address}</#if>
* ${_("permission.researchOrganization", "for")}: ${permission.researchOrganization}
* ${_("permission.researchSubject", "Subject")}:
${permission.researchSubject}
* ${_("permission.request", "Request for materials")}:
${permission.request}
<#if permission.explanation??>* ${_("permission.explanation", "Explanation")}:
${permission.explanation}</#if>
* ${_("permission.record", "Item")}: ${permission.record.toString()}
</@mailRaedingRoom>
