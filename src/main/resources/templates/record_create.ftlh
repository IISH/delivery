<#include "base.ftl"/>

<#assign title=_("newrecord.title", "Create record")/>

<@preamble title/>
<@heading/>
<@body>
    <h1>${title}</h1>

    <p>${_("newrecord.description")}</p>

    <#if e??><p>${e}</p></#if>

    <form action="" method="POST">
        <fieldset class="record_form">
            <ul class="form">
                <li><label for="title" class="field">${_("record.title")}</label><input type="text" name="title" id="title" value=""/></li>
                <li><label for="signature" class="field">${_("holding.signature")}</label><input type="text" name="signature" id="signature" value=""></li>
                <li><label for="recordSubmit" class="field"> </label><input type="submit" id="recordSubmit" name="recordSubmit" value="${_("editrecord.submit", "Submit")}"/></li>
            </ul>
        </fieldset>
    </form>
</@body>
