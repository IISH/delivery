<#include "mail.ftl">
<@mail to="${permission.name}">
<@_ "permissionMail.confirmationMessage" "With this mail we confirm your permission request has been successfully received. We will notify you when your request has either been approved or refused."/>


* <@_ "permission.name" "Name" />: ${permission.name}
* <@_ "permission.email" "E-mail" />: ${permission.email}
<#if permission.address??>* <@_ "permission.address" "Address" />:
${permission.address}</#if>
* <@_ "permission.researchOrganization" "for" />: ${permission.researchOrganization}
* <@_ "permission.researchSubject" "Subject" />:
${permission.researchSubject}
<#if permission.explanation??>* <@_ "permission.explanation" "Explanation" />:
${permission.explanation}</#if>

--- <@_ "reservation.records" "Records"/> ---
<#list permission.recordPermissions as rp>
* ${rp.record.toString()}
</#list>
</@mail>
