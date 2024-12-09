# DTKit 2 API

DTKit is a set of libraries and tools for converting input files (from different tools) into output files with a standard formats (JUNIT or TUSAR).

DTKit stands for Data Transformation Kit.It includes a set of default transformation.

Extensible

The DTKit architecture is extensible so that he end user can extend it with his own tools.

Have a look at the [DTKit Design](https://github.com/jenkinsci/dtkit-plugin/tree/master/dtkit/design/README.md) for more details.

## JUnit

JUNIT is the standard Java xUnit framework. Running JUnit framework provides result files in a JUnit format. Jenkins can record JUnit test results and provide useful information about test results, such as the historical test result trends, web UI for viewing test reports, tracking failures, and so on.
DTKit provides conversion in JUnit files with a set of xUnit result files (from C/C++,.NET or Ada xUnit frameworks) and the recording is performed by the Jenkins xUnit plugin.

## TUSAR

TUSAR stands for Thales Unified Sofware Analysis Report.
It’s a generic metric format composed of 4 categories:

* Coverage
* Measure
* Test
* Violations

TUSAR is defined by an XSD and a set of associated JAVA files.
The TUSAR XSD can be found in the Jenkins primary branch

## DTKIT FRMK

DTKIT is a set of API. It is composed of a standard XSD for JUnit and TUSAR outputs.
t 
DTKIT FRMK will be used if you want to add conversion for your own tools.
