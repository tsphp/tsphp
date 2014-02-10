# Type-Safe PHP: A compile time approach

This project aims to provide you a type-safe variant of PHP, namely TSPHP.

A transcompiler is built which [parses](https://github.com/tsphp/tsphp-parser "Parser component") your tsphp code, [type checks](https://github.com/tsphp/tsphp-typechecker "type checker component") it to ensure type safety and finally [translates](https://github.com/tsphp/tsphp-translators-php54 "translator component") it to PHP.
You can find the latest [demo application](http://tsphp.ch/jenkins/job/TSPHP_dev/lastSuccessfulBuild/) on the build server.

Please visit the [project's website](http://tsphp.ch/) for more information on the project.
Use the project's [issue tracking system](http://tsphp.ch/jira) (JIRA) to report bugs or submit feature requests.

<br/>

---

Copyright 2013 Robert Stoll <rstoll@tutteli.ch>

Licensed under the Apache License, Version 2.0 (the "License");  
you may not use this file except in compliance with the License.  
You may obtain a copy of the License at  

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software  
distributed under the License is distributed on an "AS IS" BASIS,  
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
See the License for the specific language governing permissions and  
limitations under the License.