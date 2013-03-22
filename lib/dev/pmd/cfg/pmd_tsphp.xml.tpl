<?xml version="1.0"?>
<!-- 
 Copyright 2012 Robert Stoll <rstoll@tutteli.ch> 
 
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
<ruleset name="TSPHP"
    xmlns="http://pmd.sf.net/ruleset/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://pmd.sf.net/ruleset/1.0.0 http://pmd.sf.net/ruleset_xml_schema.xsd"
    xsi:noNamespaceSchemaLocation="http://pmd.sf.net/ruleset_xml_schema.xsd">
  <description>PMD Ruleset for TSPHP</description>
  <rule ref="@DIR@/rulesets/basic.xml"/>
  <rule ref="@DIR@/rulesets/braces.xml" />
  <rule ref="@DIR@/rulesets/clone.xml" />
  <rule ref="@DIR@/rulesets/codesize.xml" />
  <rule ref="@DIR@/rulesets/coupling.xml" />
  <rule ref="@DIR@/rulesets/design.xml" />
  <rule ref="@DIR@/rulesets/finalizers.xml" />
  <rule ref="@DIR@/rulesets/imports.xml" />
  <rule ref="@DIR@/rulesets/naming.xml" />
  <rule ref="@DIR@/rulesets/optimizations.xml" />
  <rule ref="@DIR@/rulesets/strings.xml" />
  <rule ref="@DIR@/rulesets/typeresolution.xml" />
  <rule ref="@DIR@/rulesets/unusedcode.xml"/>
</ruleset>
