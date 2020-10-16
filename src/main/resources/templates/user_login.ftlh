<#include "base.ftl"/>

<#assign login=_("security.login", "Login")/>

<@preamble login/>
<@userHeading/>
<@body>
  <h1>${login}</h1>

  <#if error??>
    <ul class="errors">
      <li>
        <b>${error}</b>
      </li>
    </ul>
  </#if>

  <section>
    <form name="f" action="/user/login" method="POST">
      <fieldset>
        <label for="username" class="field">
          ${_("security.username", "Username")}
        </label>

        <input type="text" class="field" name="username" id="username"/>

        <label for="password" class="field">
          ${_("security.password", "Password")}
        </label>

        <input type="password" class="field" name="password" id="password"/>
      </fieldset>

      <ul class="buttons">
        <li>
          <input type='submit' id="submit" value='${login}'/>
        </li>
      </ul>
    </form>
  </section>
</@body>
