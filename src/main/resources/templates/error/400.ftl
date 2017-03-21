<#--

    Copyright (C) 2013 International Institute of Social History

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->

<#include "../base.ftl"/>
<#include "../form.ftl"/>

<@userbase "Error">
<h1><@_ "delivery.error" "Oops! An error has occurred."/></h1>
    <@_ "delivery.badRequest" "A bad request has occurred!" />
    <#assign email>
        <@_ "iisg.email" ""/>
    </#assign>
<p><a href="mailto:${email}">${email}</a></p>
</@userbase>
