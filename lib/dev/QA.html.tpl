<html>
<head>
<title>QA Overview @PROJ_NAME@</title>
<style type="text/css">
body{
  font-family: Arial;
  font-size:12px;
}
h1{
  font-size:1.5em;
}
.component{
  width:49%;
  float:left;
}
</style>
<head>
<body>
<h1>Quality assurance overview for component @PROJ_NAME@</h1>
    <div>
        <div class="component"><a href="checkstyle/index.html">checkstyle</a></div>
        <div class="component" style="margin-left:1%"><a href="cpd/index.html">cpd</a></div>
        <iframe class="component" src="checkstyle/index.html" style="height:40%"></iframe>
        <iframe class="component" src="cpd/index.html" style="height:40%"></iframe>
    </div>
    <div style="clear:both">
        <div class="component" style="margin-top:10px"><a href="findbugs/index.html">findbugs</a></div>
        <div class="component" style="margin-left:1%;margin-top:10px"><a href="pmd/index.html">pmd</a></div>
        <iframe class="component" src="findbugs/index.html" style="height:40%"></iframe>
        <iframe class="component" src="pmd/index.html" style="height:40%"></iframe>
    </div>
    <div style="color:#AAA;clear:both">Copyright 2013 Robert Stoll <a style="color:#AAA" href="mailto:rstoll@tutteli.ch">rstoll@tutteli.ch</a> - Licensed under Apache 2.0</div>
</body>