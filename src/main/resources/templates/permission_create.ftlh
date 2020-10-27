<#include "base.ftl"/>
<#include "form.ftl"/>

<#assign title=_("permission.create", "Create Permission Request")/>

<@userbase title>
  <h1>${title}</h1>

  <p>${_("permission.note", "")}</p>

  <section>
    <@form "" "permission" "create">
      <heading>
        <hgroup>
          <fieldset>
            <ul class="recordDetails">
              <#assign info = record.externalInfo>

              <#if record.title??>
                <li>
                  <span>${_("record.title", "Title")}</span> ${record.title}
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
          </fieldset>
        </hgroup>
      </heading>

      <div class="permission_form">
        <fieldset>
          <@input "permission.visitorName" ""/>
          <@input "permission.visitorEmail" ""/>
          <@textarea "permission.address" "" false/>
          <@input "permission.researchOrganization" ""/>
          <@textarea "permission.researchSubject" ""/>

          <label for="permission.request" class="field">
            ${_('permission.request')}
            <span class="red">*</span>

            <span class="note">
              ${_("permission.request.extra", "")}
              <a href="mailto:${_("iisg.email", "")}">${_("iisg.email", "")}</a>
            </span>
          </label>
          <@textarea_nolabel "permission.request" ""/>

          <@textarea "permission.explanation" "" false/>

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
