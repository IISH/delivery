<#include "mail.ftl">
<@mail to="${permission.name}">
${_("permissionMail.approvedMessage", "Your permission request has been (partially) approved. You can create a reservation with the code below.")}

${_("permission.item", "Item")}: ${permission.record.toString()}
${_("permissionMail.code", "Code")}: ${permission.code}
<#if permission.motivation??>

${permission.motivation}
</#if>

${_("permission.invNosGranted", "Permission for items")}: ${permission.invNosGranted?join(', ')}
</@mail>
