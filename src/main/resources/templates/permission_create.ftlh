<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=_("permission.create", "Create Permission Request")/>

<@userbase title>
  <h1>${title}</h1>

  <p>${_("createpermission.note", "")}</p>

  <section>
    <@form "" "permission" "create">
      <heading>
        <hgroup>
          <fieldset>
            <legend>${_("permission.records", "")}</legend>
              <#assign skip = []>
              <#list records as r>
                  <#assign info = r.externalInfo>
                  <#if !skip?seq_contains(r.pid)>
                    <h3>${info.title}</h3>

                    <ul class="holdingDetails">
                      <li>
                        <span>${_("record.externalInfo.materialType", "Material Type")}</span>
                          ${_("record.externalInfo.materialType.${info.materialType}", "")}
                      </li>

                        <#if r.parent??>
                          <li>
                            <span>${_("record.items", "Items")}</span>

                              <#assign items = []>
                              <#list records as childR>
                                  <#if childR.parent.pid == r.parent.pid>
                                      <#assign skip = skip + [childR.pid]>
                                      <#assign items = items + [childR.holdings[0].signature]>
                                  </#if>
                              </#list>

                              ${items?join(', ')}
                          </li>
                        </#if>

                        <#if info.author??>
                          <li>
                            <span>${_("record.externalInfo.author", "Author")}</span> ${info.author}
                          </li>
                        </#if>

                        <#if info.displayYear??>
                          <li>
                            <span>${_("record.externalInfo.displayYear", "Year")}</span> ${info.displayYear}
                          </li>
                        </#if>
                    </ul>
                  </#if>
              </#list>
          </fieldset>
        </hgroup>
      </heading>

      <div class="permission_form">
        <fieldset>
            <@input "permission.visitorName" ""/>
            <@input "permission.visitorEmail" ""/>
            <@textarea "permission.address" "" false/>
            <@input "permission.researchOrganization" "create"/>
            <@textarea "permission.researchSubject" "create"/>
            <@textarea "permission.explanation" "create" false/>

          <label for="captcha_response_field" class="field">
              ${_("captcha.explanation", "Type the following word to prevent spam")}
            <span class="red">*</span>
          </label>

          <div id="captcha_widget_div">
            <input type="text" id="captcha_response_field" name="captcha_response_field" value="" class="field"
                   autocomplete="off"/>
            <img src="<@spring.url relativeUrl="/captcha"/>" id="captcha_image"/>
            <a href="#" class="refreshCaptcha">
                ${_("captcha.refresh", "Refresh captcha")}
            </a>
          </div>

          <#if captchaError?? >
            <ul class="errors">
              <li>
                <b>${captchaError}</b>
              </li>
            </ul>
          </#if>
        </fieldset>

        <@buttons>
          <@submit "permission"/>
        </@buttons>
      </div>
    </@form>
  </section>
</@userbase>
