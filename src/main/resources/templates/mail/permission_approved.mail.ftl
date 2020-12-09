<#include "mail.ftl">
<@mail to="${permission.name}">
${_("permissionMail.approvedMessage", "Your permission request has been (partially) approved. You can create a reservation with the code below.")}

${_("permission.item", "Item")}: ${permission.record.toString()}
${_("permissionMail.code", "Code")}: ${permission.code}
<#if permission.motivation??>

${permission.motivation}
</#if>

<#if permission.invNosGranted?size != 0 && permission.invNosGranted?seq_contains('*')>
${_("permission.invAllGranted", "Permission for all inventory numbers")}
<#elseif permission.invNosGranted?size != 0>
${_("permission.invNosGranted", "Permission for inventory numbers")}: ${permission.invNosGranted?join(', ')}
</#if>

${_("permissionMail.noticeMessage", "Please note that you only have permission for the inventory numbers specified above.")}
</@mail>
