This is the source tree for the Fedora Generic Search service.
It currently works with Fedora 3.0.

To build the downloadable war, which includes
support for Lucene and Zebra plugins:

  cd FedoraGenericSearch
  ant builddownload

After building the downloadable war, documentation
can be viewed locally from FgsBuild/webapp/index.html

To clean up all build-generated files:

  cd FedoraGenericSearch
  ant clean

For more information, see the following website:
http://defxws2006.cvt.dk/fedoragsearch/
