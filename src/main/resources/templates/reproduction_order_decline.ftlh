<#include "base.ftl">

<#assign title=_("reproduction.order.decline", "Payment was declined")/>

<@userbase title>
  <section>
    <heading>
      <h1>${title}</h1>
    </heading>

    <section>
      <p>${_("reproduction.order.decline.message", "Unfortunately, your payment has been declined. Please try to finish your payment at a later moment or try a different payment method.")}</p>
    </section>

    <p>${_("reproduction.backToSearch", "Close this tab and return to Search for new requests")}</p>
  </section>
</@userbase>
