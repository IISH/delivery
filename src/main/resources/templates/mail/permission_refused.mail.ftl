<#include "mail.ftl">
<@mail to="${permission.name}">
${_("permissionMail.refusedMessage", "We are sorry, but the permission request has been refused by the owner of the archive.")}

${_("permission.item", "Item")}: ${permission.record.toString()}
<#if permission.motivation??>

${permission.motivation}
</#if>
</@mail>
